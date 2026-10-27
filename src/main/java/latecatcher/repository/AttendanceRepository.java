package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Member;
import latecatcher.model.MonthlySummary;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// 출결 저장·조회 (L-05, L-08, L-09)
public class AttendanceRepository {
    private static final String QUERY_FILE =
            "src/main/java/latecatcher/mapper/attendance-query.xml";

    // MySQL 오류 번호 1062 = UNIQUE 위반. 여기서는 같은 회원·같은 날짜 출결 중복 (uq_attendance_tbl_member_date)
    private static final int DUPLICATE_KEY = 1062;

    private final Properties prop = new Properties();

    public AttendanceRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)) {
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("출결 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    // 출결 1건을 저장한다. 저장하면 true, 같은 날짜 기록이 이미 있으면 false (L-05 예외 5a)
    // source는 DB 기본값 MANUAL, attendance_id는 AUTO_INCREMENT에 맡긴다
    // 그 밖의 DB 오류는 IllegalStateException으로 올린다 (Application이 안내 메시지로 바꾼다)
    public boolean save(Attendance attendance) {
        String sql = prop.getProperty("insertAttendance");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, attendance.getMemberId());
            pstmt.setDate(2, Date.valueOf(attendance.getAttendDate()));
            pstmt.setTime(3, attendance.getCheckIn() == null ? null : Time.valueOf(attendance.getCheckIn()));
            pstmt.setTime(4, attendance.getCheckOut() == null ? null : Time.valueOf(attendance.getCheckOut()));
            pstmt.setString(5, attendance.getStatus().name());
            pstmt.setString(6, attendance.getMemo());

            return pstmt.executeUpdate() == 1; // 1행이 들어가면 저장 성공

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == DUPLICATE_KEY) { // 1062: 같은 날짜 기록이 이미 있음 (5a)
                return false;
            }
            throw new IllegalStateException("출결 저장 중 제약 조건 오류가 발생했습니다", e);
        } catch (SQLException e) {
            throw new IllegalStateException("출결 저장 중 오류가 발생했습니다.", e);
        }
    }

    // 기간(start ~ end) 안 회원의 출결 개수를 상태별로 구한다 (L-08 기본 흐름 2)
    // - 휴일·휴강일(is_class_day = FALSE)에 들어간 기록은 개수에 포함하지 않는다
    // - 기록이 없는 상태도 0으로 넣어 돌려준다. 그래서 받는 쪽이 null 확인 없이 get으로 꺼낸다
    // - 모든 값을 더하면 기록 일수가 된다
    public Map<AttendanceStatus, Integer> countByStatus(int memberId, LocalDate start, LocalDate end) {
        String sql = prop.getProperty("countStatusByPeriod");

        Map<AttendanceStatus, Integer> counts = new EnumMap<>(AttendanceStatus.class);
        for (AttendanceStatus status : AttendanceStatus.values()) {
            counts.put(status, 0);
        }

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setDate(2, Date.valueOf(start));
            pstmt.setDate(3, Date.valueOf(end));

            try (ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    // status ENUM과 AttendanceStatus 상수 이름이 같아서 valueOf로 바로 바꾼다
                    counts.put(AttendanceStatus.valueOf(rset.getString("status")), rset.getInt("cnt"));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("출결 조회 중 오류가 발생했습니다.", e);
        }

        return counts;
    }

    // 조회 월에 기록이 1건 이상인 훈련생의 월별 집계 (L-09 기본 흐름 2, v_monthly_attendance 뷰)
    // - 기록이 없는 훈련생은 뷰에 행이 없어서 여기 나오지 않는다 → findMembersWithoutRecord로 따로 찾는다
    // - 회원 번호 순으로 돌려준다. 출석률 순위 정렬은 AttendanceController가 한다
    public List<MonthlySummary> findMonthlySummaries(YearMonth month) {
        String sql = prop.getProperty("selectMonthlySummary");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, month.toString()); // YearMonth.toString()은 뷰의 ym과 같은 "2026-10" 형식

            try (ResultSet rset = pstmt.executeQuery()) {
                return toSummaries(rset);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("월별 출결 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 조회 월 출석률이 장려금 기준 미달인 훈련생 (L-09 기본 흐름 3, HAVING)
    // - partialPerAbsence·threshold는 findCourse()의 과정 설정값을 받는다 (고정 80이 아니다)
    // - Java 계산(출석률 < 기준)과 같은 명단이 나와야 한다. 두 결과 비교는 이슈 #11 확인 항목
    public List<MonthlySummary> findBelowThreshold(YearMonth month, int partialPerAbsence, int threshold) {
        String sql = prop.getProperty("selectBelowThreshold");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // 파라미터 순서가 SQL의 ? 순서와 같아야 한다: 환산 기준 → 조회 월 → 장려금 기준
            pstmt.setInt(1, partialPerAbsence);
            pstmt.setString(2, month.toString());
            pstmt.setInt(3, threshold);

            try (ResultSet rset = pstmt.executeQuery()) {
                return toSummaries(rset);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("기준 미달 명단 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 기간(start ~ end) 안에 기록이 하나도 없는 훈련생 (L-09 기본 흐름 4, LEFT JOIN + IS NULL)
    // - 순위에서 조용히 빼지 않고 "기록 없음: N명"으로 보여 주기 위해 찾는다
    // - 이름·휴대폰 번호만 채운 Member를 돌려준다 (나이는 쓰지 않아 0)
    public List<Member> findMembersWithoutRecord(LocalDate start, LocalDate end) {
        String sql = prop.getProperty("selectMembersWithoutRecord");
        List<Member> members = new ArrayList<>();

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    Member member = new Member();
                    member.setMemberId(rset.getInt("member_id"));
                    member.setName(rset.getString("name"));
                    member.setPhone(rset.getString("phone"));
                    members.add(member);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("기록 없는 훈련생 조회 중 오류가 발생했습니다.", e);
        }

        return members;
    }

    // 뷰 컬럼을 MonthlySummary로 옮긴다. findMonthlySummaries·findBelowThreshold가 같은 컬럼을 돌려줘서 함께 쓴다
    private List<MonthlySummary> toSummaries(ResultSet rset) throws SQLException {
        List<MonthlySummary> summaries = new ArrayList<>();

        while (rset.next()) {
            summaries.add(new MonthlySummary(
                    rset.getInt("member_id"),
                    rset.getString("name"),
                    rset.getString("ym"),
                    rset.getInt("record_cnt"),
                    rset.getInt("present_cnt"),
                    rset.getInt("late_cnt"),
                    rset.getInt("early_cnt"),
                    rset.getInt("outing_cnt"),
                    rset.getInt("absent_cnt"),
                    rset.getInt("excused_cnt")
            ));
        }
        return summaries;
    }

}
