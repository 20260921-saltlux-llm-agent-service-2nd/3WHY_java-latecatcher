package latecatcher.controller;

import latecatcher.model.Member;
import latecatcher.repository.MemberRepository;
import latecatcher.view.MemberView;

// 사용자 입력과 repository/view 사이의 흐름을 조정한다.
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberView memberView;
    private Member loginMember;

    // member_tbl의 ck_member_tbl_phone CHECK와 같은 정규식
    private static final String PHONE_PATTERN = "^010-[0-9]{4}-[0-9]{4}$";

    public MemberController() {
        memberRepository = new MemberRepository();
        memberView = new MemberView();
    }

    /** L-01 회원가입 **/
    public void signUp(String name, int age, String phone) {
        // 1. 빈 값과 나이 검사
        if (name == null || name.isEmpty() || phone == null || phone.isEmpty()) {
            memberView.displayMessage("이름과 휴대폰 번호를 입력해 주세요.");
            return;
        }
        if (age <= 0) {
            memberView.displayMessage("올바른 나이를 입력해 주세요.");
            return;
        }

        // 2. 휴대폰 번호 형식 검사 (DB CHECK와 같은 정규식)
        if (!isValidPhone(phone)) {
            memberView.displayMessage("010-1234-5678 형식으로 입력해 주세요.");
            return;
        }

        // 3. 이미 등록된 휴대폰 번호인지 검사
        if (memberRepository.findByPhone(phone) != null) {
            memberView.displayMessage("이미 등록된 휴대폰 번호입니다.");
            return;
        }

        // 4. 저장 결과에 따라 가입 성공 또는 실패 메시지를 출력
        if (memberRepository.save(new Member(0, name, age, phone))) {
            memberView.displayMessage("가입되었습니다.");
            return;
        }
        memberView.displayMessage("가입에 실패했습니다.");
    }


    /*
     * L-02 휴대폰 번호 로그인
     *
     * memberRepository.findByPhone(phone)으로 회원을 조회한다.
     * 회원이 존재하면 loginMember에 저장하고 환영 메시지를 출력한 뒤 true를 반환한다.
     * 회원이 없으면 "등록되지 않은 휴대폰 번호입니다."를 출력하고 false를 반환한다.
     */
    public boolean login(String phone) {

        if (!isValidPhone(phone)) {
            memberView.displayMessage("010-1234-5678 형식으로 입력해 주세요.");
            return false;
        }

        Member member = memberRepository.findByPhone(phone);

        if (member == null) {
            memberView.displayMessage("등록되지 않은 휴대폰 번호입니다.");
            return false;
        }

        loginMember = member;
        memberView.displayMessage(loginMember.getName() + "님, 환영합니다.");
        return true;
    }

    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_PATTERN);
    }


    /*
     * L-03 과정 설정
     * 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력
     * */
    public boolean addCourse() {
        return true;
    }


    /*
     * L-04 출결 입력
     * 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력
     * */


    /*
     * L-05 출결 입력
     * 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력
     * */


    /*
     * L-08 내 출결 리스크
     * 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력
     * */


    /*
     * L-09 반 전체 현황
     * 현재 loginMember를 null로 변경하고 "로그아웃되었습니다."를 출력한다.
     * */


    /*
     * L-10 로그아웃
     * 현재 loginMember를 null로 변경하고 "로그아웃되었습니다."를 출력한다.
     */


    /** L-11 회원 탈퇴 **/
    public boolean deleteMyAccount() {

        // 로그인 (팀원 담당) → loginMember에 본인 정보 저장
        if (loginMember != null) {
            memberView.displayMessage("로그인 후 이용해주세요..");
        }

        // 탈퇴 선택 → loginMember의 번호를 deleteById()에 전달
        boolean delete = memberRepository.deleteById(loginMember.getMemberId());

        // DB에서 그 번호의 회원 삭제 → 성공하면 true
        if (delete) {
            loginMember = null;
            memberView.displayMessage("회원 탈퇴가 완료되었습니다.");
            return true;
        } else {
            memberView.displayMessage("회원 탈퇴에 실패했습니다.");
            return false;
        }
    }

}
