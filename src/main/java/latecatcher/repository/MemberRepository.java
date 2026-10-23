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

    /*
     * 회원가입
     *
     * Member의 name, age, phone을 member_tbl에 INSERT한다.
     * member_id는 AUTO_INCREMENT이므로 INSERT 컬럼에서 제외한다.
     * JDBCTemplate으로 Connection을 얻고 PreparedStatement를 사용한다.
     * executeUpdate() 결과가 1이면 true, 0이면 false를 반환한다.
     * SQL은 prop.getProperty("insertMember")로 조회한다.
     * JDBC 자원은 try-with-resources로 닫고 SQLException은 원인을 포함한
     * IllegalStateException으로 변환한다.
     */
    public boolean save(Member member) {
        return false;
    }

    /* 회원 탈퇴
     *
     * member_id가 일치하는 회원을 member_tbl에서 DELETE한다.
     * 회원의 출석 기록은 외래 키의 ON DELETE CASCADE 제약 조건으로 함께 삭제된다.
     * PreparedStatement를 사용하고 executeUpdate() 결과가 1이면 true를 반환한다.
     * JDBC 자원은 try-with-resources로 닫고 SQLException은 원인을 포함한
     * IllegalStateException으로 변환한다.
     * SQL은 prop.getProperty("deleteMemberById")로 조회한다.
     */
    public boolean deleteById(int memberId) {
        return false;
    }
}
