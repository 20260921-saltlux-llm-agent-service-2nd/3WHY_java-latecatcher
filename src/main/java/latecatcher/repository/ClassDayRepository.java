package latecatcher.repository;

import latecatcher.common.JDBCTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;

// 수업일 캘린더 조회·생성 (L-04, L-05)
public class ClassDayRepository {
    private static final String QUERY_FILE =
            "src/main/java/latecatcher/mapper/classday-query.xml";

    private final Properties prop = new Properties();

    public ClassDayRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)) {
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("수업일 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    // L-05 #20에서 추가 (YY). 그 날짜가 수업일이면 true (L-05 예외 3a)
    // 캘린더에 없으면(주말·과정 기간 밖) false, 있어도 is_class_day = FALSE(공휴일·휴강)면 false
    public boolean isClassDay(LocalDate date) {
        String sql = prop.getProperty("selectIsClassDay");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_class_day");
                }
                return false;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("수업일 조회 중 오류가 발생했습니다.", e);
        }
    }
}
