package skeleton.controller;

import skeleton.model.Member;
import skeleton.model.Membership;
import skeleton.repository.MemberRepository;
import skeleton.repository.MembershipRepository;
import skeleton.view.MemberView;

import java.util.List;

// 사용자 입력과 repository/view 사이의 흐름을 조정한다.
public class MemberController {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final MemberView memberView;

    private Member loginMember; // 로그인 상태

    public MemberController() {
        memberRepository = new MemberRepository();
        membershipRepository = new MembershipRepository();
        memberView = new MemberView();
    }

    /*
     * 문제 CONTROLLER-1. 회원가입
     *
     * 이름과 휴대폰 번호가 비어 있지 않고 나이가 1 이상인지 검사한다.
     * findByPhone(phone) 결과가 있으면 "이미 등록된 휴대폰 번호입니다."를 출력한다.
     * 중복 회원이 없으면 ID가 0인 Member를 생성해 memberRepository.save()로 저장한다.
     * 저장 결과에 따라 가입 성공 또는 실패 메시지를 출력한다.
     */
    public void signUp(String name, int age, String phone) {
        // TODO: 입력값과 휴대폰 번호 중복을 확인한 뒤 신규 회원을 저장하세요.

        // 1. 입력값 검사
        if (name == null || name.isEmpty() || age < 1 || phone == null || phone.isEmpty()) {
            memberView.displayMessage("이름과 휴대폰 번호는 비어있을 수 없습니다. 나이는 1 이상이어야 합니다.");
            return;
        }

        // 2. 폰 번호 중복 확인
        if (memberRepository.findByPhone(phone) != null) {
            memberView.displayMessage("이미 등록된 휴대폰 번호입니다.");
            return;
        }

        // 3. 회원 저장
        Member member = new Member(0, name, age, phone);

        // save()가 true면 성공, false면 실패
        if (memberRepository.save(member)) {
            memberView.displayMessage("회원가입이 완료되었습니다.");
        } else {
            memberView.displayMessage("회원가입에 실패했습니다.");
        }
    }

    /*
     * 문제 CONTROLLER-2. 휴대폰 번호 로그인
     *
     * memberRepository.findByPhone(phone)으로 회원을 조회한다.
     * 회원이 존재하면 loginMember에 저장하고 환영 메시지를 출력한 뒤 true를 반환한다.
     * 회원이 없으면 "등록되지 않은 휴대폰 번호입니다."를 출력하고 false를 반환한다.
     */
    public boolean login(String phone) {
        // TODO: 휴대폰 번호로 로그인 상태를 설정하세요.
         loginMember = memberRepository.findByPhone(phone);

        if (memberRepository != null) {
            memberView.displayMessage("환영 합니다." + loginMember);
            return true;
        } else {
            memberView.displayMessage("등록되지 않은 휴대폰 번호입니다.");
            return false;
        }
    }

    /*
     * 문제 CONTROLLER-3. 신규 구독 추가
     *
     * ottType과 planName이 비어 있지 않고 subscriptionDays가 1 이상인지 검사한다.
     * 로그인 회원 ID와 입력값으로 repository의 구독 추가 메서드를 호출한다.
     * 성공하면 "구독이 추가되었습니다."를 출력한다.
     * 실패하면 "구독 추가에 실패했습니다."를 출력한다.
     */
    public void addMyMembership(String ottType, String planName, int subscriptionDays) {
        // TODO: 입력값을 검증하고 로그인 회원의 신규 구독을 추가하세요.
    }

    /*
     * 문제 CONTROLLER-4. 내 현재 구독 정보 조회
     *
     * 로그인 회원의 ID로 현재 구독 목록을 조회한다.
     * 결과가 비어 있으면 "현재 이용 중인 구독이 없습니다."를 출력한다.
     * 결과가 있으면 MemberView의 구독 목록 출력 메서드로 전달한다.
     */
    public void displayMyActiveMemberships() {
        // TODO: 로그인 회원의 현재 구독 목록을 화면에 전달하세요.
        List<Membership> memberships =
                membershipRepository.findActiveMembershipsByMemberId(loginMember.getMemberId());

        if(memberships.isEmpty()){
            memberView.displayMessage("현재 이용 중인 구독이 없습니다.");
        } else {
            memberView.displayMemberships(memberships);
        }

    }

    /*
     * 문제 CONTROLLER-5. 선택용 전체 구독 기록 조회
     *
     * 로그인 회원의 전체 구독 기록을 조회한다.
     * 결과가 비어 있으면 "선택할 수 있는 구독 기록이 없습니다."를 출력하고 false를 반환한다.
     * 결과가 있으면 ID와 상태가 보이는 View 메서드로 전달하고 true를 반환한다.
     * Application은 true일 때만 연장 또는 삭제할 구독 ID를 입력받는다.
     */
    public boolean displayMyMembershipHistory() {
        // TODO: 선택 가능한 전체 구독 기록을 출력하고 존재 여부를 반환하세요.


        return false;
    }

    /*
     * 문제 CONTROLLER-6. 내 구독 연장
     *
     * extensionDays가 1 이상인지 검사한다.
     * 로그인 회원 ID와 함께 repository의 연장 메서드를 호출한다.
     * 성공하면 "구독 기간이 연장되었습니다."를 출력한다.
     * 실패하면 "연장할 수 있는 내 구독을 찾지 못했습니다."를 출력한다.
     */
    public void extendMyMembership(int membershipId, int extensionDays) {
        // TODO: 입력값을 검증하고 로그인 회원 본인의 구독을 연장하세요.
    }

    /*
     * 문제 CONTROLLER-7. 내 구독 기록 삭제
     *
     * 로그인 회원 ID와 함께 repository의 삭제 메서드를 호출한다.
     * 성공하면 "구독 기록이 삭제되었습니다."를 출력한다.
     * 실패하면 "삭제할 수 있는 내 구독을 찾지 못했습니다."를 출력한다.
     */
    public void deleteMyMembership(int membershipId) {
        // TODO: 로그인 회원 본인의 구독 기록을 삭제하세요.
    }

    /*
     * 문제 CONTROLLER-8. OTT별 이용했던 플랜 목록 조회
     *
     * 로그인 회원 ID로 OTT별 플랜 목록을 조회한다.
     * 결과가 비어 있으면 "구독 이력이 없습니다."를 출력한다.
     * 결과가 있으면 MemberView의 OTT별 플랜 출력 메서드로 전달한다.
     */
    public void displayMyPlanHistoryByOtt() {
        // TODO: 로그인 회원의 OTT별 플랜 이력을 화면에 전달하세요.
    }

    /*
     * 문제 CONTROLLER-9. 회원 탈퇴
     *
     * 로그인 여부를 확인한 뒤 memberRepository.deleteById()에 로그인 회원 ID를 전달한다.
     * 삭제에 성공하면 loginMember를 null로 변경하고 "회원 탈퇴가 완료되었습니다."를
     * 출력한 뒤 true를 반환한다.
     * 실패하면 "회원 탈퇴에 실패했습니다."를 출력하고 false를 반환한다.
     */
    public boolean deleteMyAccount() {
        // TODO: 로그인 회원을 삭제하고 성공 시 로그인 상태를 해제하세요.

        if(loginMember == null){
            memberView.displayMessage("로그인 후 이용해주세요.");
            return false;
        }

        boolean deleted = memberRepository.deleteById(loginMember.getMemberId());

        if (deleted) {
            loginMember = null;
            memberView.displayMessage("회원 탈퇴가 완료되었습니다.");
            return true;
        } else {
            memberView.displayMessage("회원 탈퇴에 실패했습니다.");
            return false;
        }
    }

    /*
     * 문제 CONTROLLER-10. 로그아웃
     *
     * 현재 loginMember를 null로 변경하고 "로그아웃되었습니다."를 출력한다.
     */
    public void logout() {
        // TODO: 로그인 상태를 해제하세요.
    }
}
