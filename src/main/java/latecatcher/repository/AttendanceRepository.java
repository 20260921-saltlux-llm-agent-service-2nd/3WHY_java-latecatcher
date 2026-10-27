package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.EnumMap;
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

}
