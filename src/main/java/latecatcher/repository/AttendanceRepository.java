package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
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

    private final Properties prop = new Properties();

    public AttendanceRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)) {
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("출결 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    // 기간(start ~ end) 안 회원의 출결을 상태별로 센다 (L-08 기본 흐름 2)
    // - 휴일·휴강일(is_class_day = FALSE)에 들어간 기록은 세지 않는다
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
