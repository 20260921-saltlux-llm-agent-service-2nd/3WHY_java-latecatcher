package skeleton.repository;

import skeleton.model.Membership;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static skeleton.common.JDBCTemplate.close;
import static skeleton.common.JDBCTemplate.getConnection;

/**
 * 구독 정보 JDBC 처리 담당.
 * <p>
 * 모든 변경 쿼리는 membership_id뿐 아니라 로그인 회원의 member_id도
 * WHERE 조건으로 사용해야 한다.</p>
 */
public class MembershipRepository {

    private static final String QUERY_FILE =
            "src/main/java/skeleton/mapper/membership-query.xml";

    private final Properties prop = new Properties();

    public MembershipRepository() {
        try (FileInputStream inputStream = new FileInputStream(QUERY_FILE)) {
            prop.loadFromXML(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("구독 SQL 파일을 읽지 못했습니다.", e);
        }
    }

    /*
     * 문제 REPOSITORY-4. 신규 구독 추가
     *
     * 로그인 회원 ID, OTT 이름, 플랜 이름, 구독 일수를 membership_tbl에 INSERT한다.
     * enroll_date와 active는 테이블의 기본값을 사용하므로 INSERT 컬럼에서 제외한다.
     * expiry_date는 현재 시각에 subscriptionDays를 더한 값으로 저장한다.
     * PreparedStatement를 사용하고 executeUpdate() 결과가 1이면 true를 반환한다.
     * SQL은 prop.getProperty("insertMembership")로 조회한다.
     */
    public boolean addMembership(int memberId,
                                 String ottType,
                                 String planName,
                                 int subscriptionDays) {
        // TODO: XML의 SQL을 사용해 로그인 회원의 신규 OTT 구독을 저장하세요.
        PreparedStatement pstmt = null;
        Connection con = getConnection();
        String newSubs = prop.getProperty("insertMembership");
        int result = 0;

        try {
            pstmt = con.prepareStatement(newSubs);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, ottType);
            pstmt.setString(3, planName);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().plusDays(subscriptionDays)));

            result = pstmt.executeUpdate();
            if (result == 1) {
                return true;
            }


        } catch (SQLException e) {
            throw new IllegalStateException("구독 추가에 실패했습니다..", e);
        } finally {
            close(pstmt);
            close(con);

        }


        return false;
    }

    /*
     * 문제 REPOSITORY-5. 현재 구독 목록 조회
     *
     * 로그인 회원의 구독 중 active가 true이고 expiry_date가 현재 시각 이상인
     * 구독을 만료일 오름차순으로 조회한다.
     * 각 행을 Membership 객체로 변환하고 List에 담아 반환한다.
     * 조회 결과가 없으면 비어 있는 List를 반환한다.
     * SQL은 prop.getProperty("selectActiveMemberships")로 조회한다.
     */
    public List<Membership> findActiveMembershipsByMemberId(int memberId) {
        // TODO: XML의 SQL을 사용해 로그인 회원의 현재 구독 목록을 조회하세요.
        String query = prop.getProperty("selectActiveMemberships");
        Connection con = getConnection();
        PreparedStatement pstmt = null;
        ResultSet rset = null;

        // 결과를 담을 비어있는 List 생성 (조회 결과가 없으면 이대로 반환됨)
        List<Membership> membershipList = new ArrayList<>();

        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, memberId);

            while (rset.next()) {
                Membership membership = new Membership();

                // 테이블의 컬럼명에 맞게 매핑
                membership.setMembershipId(rset.getInt("membership_id"));
                membership.setMemberId(rset.getInt("member_id"));
                membership.setActive(rset.getBoolean("active"));
                membership.setExpiryDate(rset.getTimestamp("expiry_date").toLocalDateTime());

                membershipList.add(membership);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // 자원 반납
            close(rset);
            close(pstmt);
            close(con);
        }
        return membershipList;
    }

    /*
     * 문제 REPOSITORY-6. 내 전체 구독 기록 조회
     *
     * 로그인 회원의 활성, 비활성, 만료 구독을 모두 조회한다.
     * 사용자가 연장하거나 삭제할 구독 ID를 알 수 있도록 모든 컬럼을 조회한다.
     * 현재 이용 중인 구독을 먼저 표시하고 만료일 내림차순으로 정렬한다.
     * 각 행을 Membership 객체로 변환하여 List로 반환한다.
     * SQL은 prop.getProperty("selectAllMemberships")로 조회한다.
     */
    public List<Membership> findAllMembershipsByMemberId(int memberId) {
        // TODO: XML의 SQL을 사용해 로그인 회원의 전체 구독 기록을 조회하세요.
        return new ArrayList<>();
    }

    /*
     * 문제 REPOSITORY-7. 내 구독 연장
     *
     * membership_id와 member_id가 모두 일치하는 구독만 수정한다.
     * 아직 만료되지 않았다면 기존 expiry_date를 기준으로 연장하고,
     * 이미 만료되었다면 현재 시각을 기준으로 extensionDays만큼 연장한다.
     * 연장된 구독의 active는 true로 변경한다.
     * executeUpdate() 결과가 1이면 true, 0이면 false를 반환한다.
     * SQL은 prop.getProperty("updateMembershipExpiry")로 조회한다.
     */
    public boolean extendMembership(int membershipId, int memberId, int extensionDays) {
        // TODO: XML의 SQL을 사용해 로그인 회원 본인의 구독만 연장하세요.
        return false;
    }

    /*
     * 문제 REPOSITORY-8. 내 구독 기록 삭제
     *
     * membership_id와 member_id가 모두 일치하는 행만 삭제한다.
     * executeUpdate() 결과가 1이면 true, 0이면 false를 반환한다.
     * SQL은 prop.getProperty("deleteMembership")로 조회한다.
     */
    public boolean deleteMembership(int membershipId, int memberId) {
        // TODO: XML의 SQL을 사용해 로그인 회원 본인의 구독 기록만 삭제하세요.
        return false;
    }

    /*
     * 문제 REPOSITORY-9. OTT별 이용했던 플랜 목록 조회
     *
     * 로그인 회원이 현재 또는 과거에 이용한 모든 구독 기록에서
     * ott_type과 plan_name 조합을 중복 없이 조회한다.
     * ott_type, plan_name 오름차순으로 정렬한다.
     * LinkedHashMap을 사용해 OTT 이름별 플랜 이름 목록으로 묶어 반환한다.
     * 조회 결과가 없으면 비어 있는 Map을 반환한다.
     * SQL은 prop.getProperty("selectPlanHistory")로 조회한다.
     */
    public Map<String, List<String>> findSubscribedPlansByOtt(int memberId) {
        // TODO: XML의 SQL을 사용해 조회 결과를 OTT별 플랜 목록으로 그룹화하세요.
        return new LinkedHashMap<>();
    }
}
