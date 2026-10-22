package skeleton.repository;

import skeleton.common.JDBCTemplate;
import skeleton.model.Member;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 회원 정보 JDBC 처리 담당.
 */
public class MemberRepository {

    private static final String QUERY_FILE =
            "src/main/java/skeleton/mapper/member-query.xml";

    private final Properties prop = new Properties();

    public MemberRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)) {
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("회원 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    /*
     * 문제 REPOSITORY-1. 회원가입
     *
     * Member의 name, age, phone을 member_tbl에 INSERT한다.
     * member_id는 AUTO_INCREMENT이므로 INSERT 컬럼에서 제외한다.
     * JdbcConnectionFactory로 Connection을 얻고 PreparedStatement를 사용한다.
     * executeUpdate() 결과가 1이면 true, 0이면 false를 반환한다.
     * SQL은 prop.getProperty("insertMember")로 조회한다.
     * JDBC 자원은 try-with-resources로 닫고 SQLException은 원인을 포함한
     * IllegalStateException으로 변환한다.
     */
    public boolean save(Member member) {
        // TODO: XML의 SQL을 사용해 JDBC로 신규 회원을 저장하세요.

        // XML에서 회원가입 SQL가져오기
        String join = prop.getProperty("insertMember");

        try (Connection con = JDBCTemplate.getConnection(); // DB연결
             PreparedStatement pstmt = con.prepareStatement(join)) { // SQL 준비

            // SQL (name, age, phone)
            pstmt.setString(1, member.getName());
            pstmt.setInt(2, member.getAge());
            pstmt.setString(3, member.getPhone());

            // INSERT 실행
            int result = pstmt.executeUpdate();

            // 회원 1명이 저장햇으면 1 / 안 됐으면 0
            return result == 1;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * 문제 REPOSITORY-2. 휴대폰 번호로 회원 조회
     *
     * member_tbl 테이블에서 phone이 매개변수와 일치하는 회원 한 명을 조회한다.
     * JdbcConnectionFactory로 Connection을 얻고 PreparedStatement를 사용한다.
     * ResultSet에 조회 결과가 있으면 Member 객체로 변환하여 반환한다.
     * 조회 결과가 없으면 null을 반환한다.
     * Connection, PreparedStatement, ResultSet은 try-with-resources로 닫는다.
     * SQLException은 메시지와 원인을 포함한 IllegalStateException으로 변환한다.
     * SQL은 prop.getProperty("selectMemberByPhone")로 조회한다.
     */
    public Member findByPhone(String phone) {
        // TODO: XML의 SQL을 사용해 휴대폰 번호가 일치하는 회원을 조회하세요.

        // XML에서 회원가입 SQL가져오기
        String select = prop.getProperty("selectMemberByPhone");

        try (Connection con = JDBCTemplate.getConnection(); // DB연결
             PreparedStatement pstmt = con.prepareStatement(select)) { // SQL 준비

            // 매개변수로 받은 phone
            pstmt.setString(1, phone);

            // 결과를 rset으로 받기
            try (ResultSet rset = pstmt.executeQuery()) {

                // 결과에 회원이 있으면
                if (rset.next()) {
                    Member member = new Member();
                    member.setMemberId(rset.getInt("member_id"));
                    member.setName(rset.getString("name"));
                    member.setAge(rset.getInt("age"));
                    member.setPhone(rset.getString("phone"));
                    return member;

                }
            }
            // 결과에 회원이 없으면
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("회원 조회에 실패했습니다.", e);
        }
    }

    /*
     * 문제 REPOSITORY-3. 회원 탈퇴
     *
     * member_id가 일치하는 회원을 member_tbl에서 DELETE한다.
     * 회원의 구독 기록은 외래 키의 ON DELETE CASCADE 제약 조건으로 함께 삭제된다.
     * PreparedStatement를 사용하고 executeUpdate() 결과가 1이면 true를 반환한다.
     * JDBC 자원은 try-with-resources로 닫고 SQLException은 원인을 포함한
     * IllegalStateException으로 변환한다.
     * SQL은 prop.getProperty("deleteMemberById")로 조회한다.
     */
    public boolean deleteById(int memberId) {
        // TODO: XML의 SQL을 사용해 로그인 회원을 삭제하세요.

        // XML에서 회원가입 SQL가져오기
        String delete = prop.getProperty("deleteMemberById");

        try (Connection con = JDBCTemplate.getConnection(); // DB연결
             PreparedStatement pstmt = con.prepareStatement(delete)) { // SQL 준비

            // 매개변수로 받은 memberId
            pstmt.setInt(1, memberId);

            // DELETE 실행
            int result = pstmt.executeUpdate();

            // 회원 1명이 지워졌으면 true / 해당 번호가 없으면 0 → false
            return result == 1;

        } catch (SQLException e) {
            throw new IllegalStateException("회원 탈퇴에 실패했습니다.", e);
        }
    }
}


