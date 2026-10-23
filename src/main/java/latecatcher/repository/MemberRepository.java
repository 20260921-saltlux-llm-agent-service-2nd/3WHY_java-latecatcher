package latecatcher.repository;

import latecatcher.model.Member;
import latecatcher.common.JDBCTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MemberRepository {
    private static final String QUERY_FILE =
            "src/main/java/latecatcher/mapper/member-query.xml";

    private final Properties prop = new Properties();

    public MemberRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)){
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("회원 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    public Member findByPhone(String phone) {
        String query = prop.getProperty("selectMemberByPhone");

        try (Connection connection = JDBCTemplate.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, phone);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Member(
                            resultSet.getInt("member_id"),
                            resultSet.getString("name"),
                            resultSet.getInt("age"),
                            resultSet.getString("phone")
                    );
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("회원 조회 중 오류가 발생했습니다.", e);
        }

        return null;
    }



}
