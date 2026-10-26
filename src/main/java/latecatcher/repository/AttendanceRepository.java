package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.Attendance;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
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


}
