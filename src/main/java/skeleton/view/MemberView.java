package skeleton.view;

import skeleton.model.Membership;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// 조회 결과와 안내 메시지 출력을 담당한다.
public class MemberView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /*
     * 문제 VIEW-1. 현재 구독 목록 출력
     *
     * 전달받은 구독을 한 줄씩 출력한다.
     * 각 줄에는 구독 ID, OTT, 플랜, 가입일, 만료일을 포함한다.
     * 날짜는 DATE_TIME_FORMATTER를 사용해 출력한다.
     */
    public void displayMemberships(List<Membership> memberships) {
        // TODO: 현재 구독 목록을 보기 좋은 형식으로 출력하세요.
        // 1. 각 구독 정보 출력
        for (Membership membership : memberships) {
            int id = membership.getMembershipId();
            String ott = membership.getOttType();
            String plan = membership.getPlanName();
            String regDateStr = membership.getEnrollDate().format(DATE_TIME_FORMATTER);
            String expiryDateStr = membership.getExpiryDate().format(DATE_TIME_FORMATTER);

            // 한 줄로 보기 좋게 출력
            System.out.printf("구독 ID: %d | OTT: %s | 플랜: %s | 가입일: %s | 만료일: %s%n",
                    id, ott, plan, regDateStr, expiryDateStr);
        }



    }

    /*
     * 문제 VIEW-2. 선택용 전체 구독 기록 출력
     *
     * 연장 또는 삭제 전에 사용자가 구독 ID를 확인할 수 있도록 전체 기록을 출력한다.
     * 각 줄에는 구독 ID, OTT, 플랜, 가입일, 만료일, 현재 상태를 포함한다.
     * active가 true이고 만료일이 현재 시각 이상이면 "이용 중",
     * 그렇지 않으면 "만료/비활성"으로 출력한다.
     */
    public void displayMembershipHistory(List<Membership> memberships) {
        // TODO: ID와 상태가 포함된 전체 구독 기록을 출력하세요.
    }

    /*
     * 문제 VIEW-3. OTT별 이용 플랜 목록 출력
     *
     * Map의 각 entry를 순회한다.
     * OTT 이름과 해당 OTT에서 이용했던 플랜 이름들을 한 줄에 출력한다.
     */
    public void displayPlanHistoryByOtt(Map<String, List<String>> planHistory) {
        // TODO: OTT별 플랜 목록을 출력하세요.
    }

    /*
     * 문제 VIEW-4. 안내 메시지 출력
     *
     * 전달받은 message를 한 줄에 출력한다.
     */
    public void displayMessage(String message) {
        // TODO: 안내 메시지를 출력하세요.
    }
}
