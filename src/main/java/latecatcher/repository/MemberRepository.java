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

    /** 회원가입 **/
    public boolean save(Member member) {
        String sql = prop.getProperty("insertMember");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, member.getName());
            pstmt.setInt(2, member.getAge());
            pstmt.setString(3, member.getPhone());

            int result = pstmt.executeUpdate();
            return result == 1;

        } catch (SQLException e) {
            throw new IllegalStateException("회원 저장에 실패했습니다.", e);
        }
    }

    /** 회원 탈퇴 **/
    public boolean deleteById(int memberId) {
       String sql = prop.getProperty("deleteMemberById");

       try(Connection con = JDBCTemplate.getConnection();
           PreparedStatement pstmt = con.prepareStatement(sql)){

            pstmt.setInt(1, memberId);

            int result = pstmt.executeUpdate();
            return result == 1;

        } catch (SQLException e) {
            throw new IllegalStateException("회원 삭제에 실패했습니다.", e);
        }
    }
}
