package latecatcher.controller;


// 사용자 입력과 repository/view 사이의 흐름을 조정한다.
public class MemberController {

    /*
     *
     * L-01 회원가입
     *
     * 이름과 휴대폰 번호가 비어 있지 않고 나이가 1 이상인지 검사한다.
     * findByPhone(phone) 결과가 있으면 "이미 등록된 휴대폰 번호입니다."를 출력한다.(가입불가)
     * 중복 회원이 없으면 ID가 0인 Member를 생성해 memberRepository.save()로 저장한다.
     * 저장 결과에 따라 가입 성공 또는 실패 메시지를 출력한다.
     */
    public void signUp(String name, int age, String phone) {}



    /*
     * L-02 휴대폰 번호 로그인
     *
     * memberRepository.findByPhone(phone)으로 회원을 조회한다.
     * 회원이 존재하면 loginMember에 저장하고 환영 메시지를 출력한 뒤 true를 반환한다.
     * 회원이 없으면 "등록되지 않은 휴대폰 번호입니다."를 출력하고 false를 반환한다.
     */
    public boolean login(String phone) {
        return true;
    }



    /*
     * L-03 과정 설정
     * 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력
     * */
    public boolean addCourse(){
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


    /*
     * L-11 회원 탈퇴
     * 로그인 여부를 확인한 뒤 memberRepository.deleteById()에 로그인 회원 ID를 전달한다.
     * 삭제에 성공하면 loginMember를 null로 변경하고 "회원 탈퇴가 완료되었습니다."를
     * 출력한 뒤 true를 반환한다.
     * 실패하면 "회원 탈퇴에 실패했습니다."를 출력하고 false를 반환한다.
     */


}