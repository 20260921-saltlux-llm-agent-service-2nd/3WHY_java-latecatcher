package latecatcher.controller;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import latecatcher.repository.AttendanceRepository;
import latecatcher.repository.ClassDayRepository;
import latecatcher.repository.CourseRepository;
import latecatcher.rule.AttendanceRisk;
import latecatcher.rule.AttendanceRule;
import latecatcher.view.AttendanceView;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

// L-08 내 출결 리스크. 조회(Repository)와 계산(AttendanceRule, AttendanceRisk)을 모아 화면(AttendanceView)에 넘긴다
public class AttendanceController {

    // 사용할 repository, 계산, view 객체를 미리 만들어둠 (ClassDayController와 같은 방식)
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private final ClassDayRepository classDayRepository = new ClassDayRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final AttendanceRule rule = new AttendanceRule();
    private final AttendanceRisk risk = new AttendanceRisk();
    private final AttendanceView view = new AttendanceView();

    /*
     * 내 출결 리스크 보기 (L-08)
     * 흐름: 과정 조회 → 조회 기간 → 출결 개수 → 미입력 → 출석률 → 경고 계산 → 화면 출력
     * - memberId: 로그인한 회원 번호 (Application이 MemberController에서 꺼내 넘긴다)
     * - courseId: 사용자가 입력한 과정 번호 (L-04 캘린더와 같은 방식)
     */
    public void showMyRisk(int memberId, int courseId) {

        // 1. 과정 조회
        //    CourseRepository는 Connection을 매개변수로 받는 방식이라
        //    여기서 연결을 열고, 다 쓰면 닫음
        Connection con = JDBCTemplate.getConnection();
        Course course = courseRepository.selectCourse(con, courseId);
        JDBCTemplate.close(con);

        // 과정이 없으면 여기서 끝냄
        if (course == null) {
            view.printMessage("존재하지 않는 과정입니다.");
            return;
        }

        // 2. 조회 기간 = 어제가 속한 달 1일 ~ 어제 (오늘은 퇴실 전이라 뺀다, PLAN 3절 계산 기준일)
        //    - 오늘이 1일이면 어제가 지난달이라 지난달 전체를 본다
        //    - 과정 시작일보다 앞이면 시작일부터, 과정 종료일보다 뒤면 종료일까지
        LocalDate baseDate = LocalDate.now().minusDays(1);
        LocalDate start = baseDate.withDayOfMonth(1);
        if (start.isBefore(course.getStartDate())) {
            start = course.getStartDate();
        }
        LocalDate end = baseDate.isAfter(course.getEndDate()) ? course.getEndDate() : baseDate;

        // 시작이 끝보다 뒤면 이번 달에 훈련한 날이 없다 (과정 시작 전이거나 이미 끝남)
        if (start.isAfter(end)) {
            view.printMessage("이번 달은 훈련 기간이 아닙니다.");
            return;
        }

        // 3. 출결 종류별 개수 → 기록 일수, 지각·조퇴·외출 환산 횟수 합계
        Map<AttendanceStatus, Integer> counts = attendanceRepository.countByStatus(memberId, start, end);
        int recordDays = 0; // 기록 일수 = 모든 종류 개수의 합
        int partial = 0;    // 환산 횟수 합 = 개수 × 종류별 환산 횟수 (지각·조퇴는 2회, AttendanceStatus 참고)
        for (Map.Entry<AttendanceStatus, Integer> entry : counts.entrySet()) {
            recordDays += entry.getValue();
            partial += entry.getValue() * entry.getKey().getPartialCount();
        }

        // 기록이 하나도 없으면 계산하지 않는다 (PLAN L-08 예외 흐름 2a)
        if (recordDays == 0) {
            view.printMessage("이번 달 기록이 없습니다 — 출결을 입력하세요");
            return;
        }

        // 4. 미입력 일수 = 수업일 수(휴일 제외) − 기록 일수. 계산에는 넣지 않고 화면에만 보여 준다
        int classDays = classDayRepository.countRealClassDays(start, end);
        int missing = rule.missingDays(classDays, recordDays);

        // 5. 환산 결석, 출석률 (AttendanceRule, PLAN 3절 공식)
        int absent = counts.get(AttendanceStatus.ABSENT);
        int excused = counts.get(AttendanceStatus.EXCUSED); // 공결은 분모·분자에서 모두 뺀다
        int converted = rule.convertedAbsence(absent, partial, course);
        OptionalDouble rate = rule.attendanceRate(recordDays, excused, converted); // 공결만 있으면 empty

        // 6. 결석 며칠 / 지각·조퇴 몇 번 더면 장려금 기준(80%) 아래인지 (AttendanceRisk)
        OptionalInt moreAbsences = risk.moreAbsencesToFail(recordDays, excused, absent, partial, course);
        OptionalInt morePartials = risk.morePartialsToFail(recordDays, excused, absent, partial, course);

        // 7. 화면 출력 (AttendanceView.printRisk는 다음 단계에서 만든다)
        view.printRisk(start, end, classDays, recordDays, missing, counts,
                converted, rate, moreAbsences, morePartials, course);
    }
}
