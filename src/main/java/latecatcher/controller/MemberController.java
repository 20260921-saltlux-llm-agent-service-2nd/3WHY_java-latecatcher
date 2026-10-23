package latecatcher.controller;

import latecatcher.model.Course;
import latecatcher.model.Member;
import latecatcher.repository.CourseRepository;
import latecatcher.repository.MemberRepository;
import latecatcher.view.MemberView;

import java.sql.Connection;

import static latecatcher.common.JDBCTemplate.close;
import static latecatcher.common.JDBCTemplate.getConnection;


// 사용자 입력과 repository/view 사이의 흐름을 조정한다.
public class MemberController {

    private int nextCourseId = 1; // 등록된 과정 자동 증가 ID 카운터
    private final CourseRepository courseRepository;
    private final MemberView memberView;
    private final MemberRepository memberRepository;

    private Member loginMember;

    // member_tbl의 ck_member_tbl_phone CHECK와 같은 정규식
    private static final String PHONE_PATTERN = "^010-[0-9]{4}-[0-9]{4}$";

    public MemberController() {
        memberRepository = new MemberRepository();
        memberView = new MemberView();
        courseRepository = new CourseRepository();
    }

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
     * - 과정명, 훈련 시작·종료일, 수업 시작·종료 시각 입력값을 검증한다.
     * - 검증 통과 시 courseRepository를 통해 과정을 등록한다.
     * - 성공하면 "과정이 성공적으로 등록되었습니다."를 출력한다.
     * - 실패하면 "과정 등록에 실패했습니다." 또는 오류 메시지를 출력한다.
     */
    public void addCourse(Course course) {
        // 1. 객체 null 체크
        if (course == null) {
            memberView.displayMessage("등록할 과정 정보가 없습니다.");
            return;
        }

        // 2. 과정명 필수값 검증
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            memberView.displayMessage("과정명은 필수 입력 항목입니다.");
            return;
        }

        // 3. 훈련 시작일 / 종료일 유효성 검증
        if (course.getStartDate() == null || course.getEndDate() == null) {
            memberView.displayMessage("훈련 시작일과 종료일을 입력해야 합니다.");
            return;
        }
        if (course.getEndDate().isBefore(course.getStartDate())) {
            memberView.displayMessage("종료일은 시작일보다 빠를 수 없습니다.");
            return;
        }

        // 4. 수업 시작 시각 / 종료 시각 유효성 검증
        if (course.getStartTime() == null || course.getEndTime() == null) {
            memberView.displayMessage("수업 시작 시각과 종료 시각을 입력해야 합니다.");
            return;
        }
        if (!course.getEndTime().isAfter(course.getStartTime())) {
            memberView.displayMessage("수업 종료 시각은 시작 시각 이후여야 합니다.");
            return;
        }

        // 5. 기준 퍼센트 범위(0~100) 검증
        if (course.getAllowanceThreshold() < 0 || course.getAllowanceThreshold() > 100 ||
                course.getCompletionThreshold() < 0 || course.getCompletionThreshold() > 100) {
            memberView.displayMessage("기준 비율(%)은 0에서 100 사이여야 합니다.");
            return;
        }

        // 6. DB 커넥션 획득 및 repository 메서드 호출
        course.setCourseId(nextCourseId); // Course 객체에 현재 카운터 번호 세팅

        // 2. DB 저장
        Connection con = getConnection();
        int result = courseRepository.insertCourse(con, course);
        close(con);

        // 3. 저장이 성공했을 때만 ID 카운터 증가
        if (result > 0) {
            nextCourseId++;
            memberView.displayMessage("과정이 성공적으로 등록되었습니다.");
        } else {
            memberView.displayMessage("과정 등록에 실패했습니다.");
        }
    }


    /*
     * L-04 수업일 캘린더
     * */


    /*
     * L-05 출결 입력
     * 날짜, 구분(출석/결석), 출석이면 입실·퇴실 시각
     * */


    /*
     * L-08 내 출결 리스크
     * 출석률 계산, 80% 미달 위험 시 경고
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