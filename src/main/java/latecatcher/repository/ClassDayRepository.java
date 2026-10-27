package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.ClassDay;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClassDayRepository {

    private final Properties prop = new Properties();

    /**
     * 생성자
     **/
    public ClassDayRepository() {
        try {
            prop.loadFromXML(new FileInputStream("src/main/java/latecatcher/mapper/classday-query.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 조회 매서드 **/
    /**
     * 해당 날짜가 수업일인지 확인
     * - class_day_tbl에 날짜가 있고 is_class_day가 TRUE면 true
     * - 날짜가 없거나 FALSE면 false
     * - 출석 입력(#20)에서 "수업일이 아닙니다" 확인에 사용
     */
    public boolean isClassDay(LocalDate date) {

        // XML에서 쿼리 꺼내기
        String sql = prop.getProperty("selectIsClassDay");

        // DB 연결 + 쿼리 준비 (try 괄호 안에 넣어 자동으로 닫힘)
        try (Connection connection = JDBCTemplate.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // ?에 확인할 날짜 넣기
            pstmt.setDate(1, Date.valueOf(date));

            // 쿼리 실행 후 결과 받기
            try (ResultSet rset = pstmt.executeQuery()) {
                // 조회된 행이 있으면 is_class_day 값 반환
                if (rset.next()) {
                    return rset.getBoolean("is_class_day");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // 해당 날짜가 테이블에 없으면 수업일 아님
        return false;
    }

    /**
     * 기간 내 이미 생성된 수업일 개수 조회
     * - 0이면 아직 생성 안 됨, 0보다 크면 이미 생성됨 (중복 생성 방지용)
     */
    public int countClassDays(LocalDate start, LocalDate end) {
        String sql = prop.getProperty("countClassDays");

        try (Connection connection = JDBCTemplate.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // BETWEEN ? AND ? 에 시작일, 종료일 넣기
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {
                    return rset.getInt(1); // COUNT(*) 결과는 첫 번째 칸
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    /**
     * 기간 내 실제 수업일 개수 조회 (L-08 내 출결 리스크)
     * - 공휴일·휴강일(is_class_day = FALSE)은 빼고 센다
     * - countClassDays는 휴일까지 세서 중복 생성 확인에만 쓰고, 미입력 계산에는 이 메서드를 쓴다
     * - 미입력 일수 = 이 값 − 기록 일수 (AttendanceRule.missingDays)
     */
    public int countRealClassDays(LocalDate start, LocalDate end) {
        String sql = prop.getProperty("countRealClassDays");

        try (Connection connection = JDBCTemplate.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // BETWEEN ? AND ? 에 시작일, 종료일 넣기
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {
                    return rset.getInt(1); // COUNT(*) 결과는 첫 번째 칸
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // COUNT(*)는 항상 1행을 돌려주므로 여기까지 오지 않는다
        return 0;
    }


    /**
     * 수업일 여러 개를 한 번에 저장
     * - 중간에 실패하면 전부 취소(rollback)
     *
     * @return 저장된 개수
     */
    public int insertClassDays(List<ClassDay> days) {
        String sql = prop.getProperty("insertClassDays");

        try (Connection connection = JDBCTemplate.getConnection()) {

            // 자동 저장 끄기: 전부 성공했을 때만 한 번에 저장하려고
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

                // 수업일 하나씩 ?에 값 넣고 묶음(batch)에 추가
                for (ClassDay day : days) {
                    pstmt.setDate(1, Date.valueOf(day.getClassDate()));
                    pstmt.setBoolean(2, day.isClassDay());
                    pstmt.setString(3, day.getNote());
                    pstmt.addBatch();
                }

                // 묶어둔 INSERT 한 번에 실행
                int[] result = pstmt.executeBatch();
                connection.commit(); // 전부 성공 → 저장 확정
                return result.length;

            } catch (SQLException e) {
                connection.rollback(); // 실패 → 전부 취소
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 기간 내 수업일 목록 조회 (캘린더용)
     * - 날짜 순서대로 정렬해서 반환
     */
    public List<ClassDay> selectClassDays(LocalDate start, LocalDate end) {
        String sql = prop.getProperty("selectClassDays");
        List<ClassDay> days = new ArrayList<>();

        try (Connection connection = JDBCTemplate.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rset = pstmt.executeQuery()) {
                // 조회된 행을 하나씩 ClassDay에 담아서 목록에 추가
                while (rset.next()) {
                    ClassDay day = new ClassDay(
                            rset.getDate("class_date").toLocalDate(),
                            rset.getBoolean("is_class_day"),
                            rset.getString("note")
                    );
                    days.add(day);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return days; // 결과가 없으면 빈 목록
    }
}