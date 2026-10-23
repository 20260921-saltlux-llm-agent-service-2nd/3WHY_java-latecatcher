package latecatcher.view;

import java.time.format.DateTimeFormatter;

// 조회 결과와 안내 메시지 출력을 담당한다.
public class MemberView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /*
     * 문제 VIEW-1. 회원가입
     *
     * 이름, 나이, 휴대폰 번호를 입력받아 MemberController의 signUp()에 전달한다.
     * 휴대폰 번호는 010-1234-5678 형식으로 입력하도록 안내한다.
     * 결과 메시지는 MemberController에서 출력한다.
     */
    public void signUpView() {}


    /*
     * 문제 VIEW-2. 회원탈퇴
     *
     * 탈퇴할 회원 번호를 입력받고, 탈퇴 여부를 Y/N으로 한 번 더 확인한다.
     * Y이면 MemberController에 전달하고, N이면 "회원탈퇴를 취소했습니다."를 출력한다.
     * 성공하면 "회원탈퇴가 완료되었습니다."를 출력한다.
     * 해당 회원이 없으면 "해당 회원이 존재하지 않습니다."를 출력한다.
     */
}
