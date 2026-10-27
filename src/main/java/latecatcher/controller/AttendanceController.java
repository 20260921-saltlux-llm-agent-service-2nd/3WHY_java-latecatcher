package latecatcher.controller;

import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import latecatcher.model.Member;
import latecatcher.repository.AttendanceRepository;
import latecatcher.repository.ClassDayRepository;
import latecatcher.repository.CourseRepository;
import latecatcher.rule.AttendanceRisk;
import latecatcher.rule.AttendanceRule;
import latecatcher.view.AttendanceView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

// 출결 입력 흐름 (L-05). 날짜·구분·시각을 확인하고 AttendanceRule로 판정해 저장한다
// 입력과 출력은 AttendanceView가 하고, 판단·판정·저장은 여기서 한다 (나중에 메신저 입력 L-15도 같은 경로를 쓴다)
// L-08 내 출결 리스크. 조회(Repository)와 계산(AttendanceRule, AttendanceRisk)을 모아 화면(AttendanceView)에 넘긴다
public class AttendanceController {
    private static final DateTimeFormatter RESULT_DATE = DateTimeFormatter.ofPattern("MM/dd");

    private final AttendanceView view;
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private final ClassDayRepository classDayRepository = new ClassDayRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final AttendanceRule rule = new AttendanceRule();
    private final AttendanceRisk risk = new AttendanceRisk();

    public AttendanceController(AttendanceView view) {
        this.view = view;
    }

    // 로그인한 회원의 출결 1건을 입력받아 저장한다. 3a·3b·4a·5a는 안내 후 날짜 입력으로 돌아간다 (PLAN 4절 L-05)
    public void inputAttendance(Member member) {
        if (member == null) {
            view.displayMessage("로그인한 뒤 출결을 입력해 주세요.");
            return;
        }
        Course course = courseRepository.findCourse();
        if (course == null) {
            view.displayMessage("과정 설정이 없습니다. 과정 설정을 먼저 해 주세요.");
            return;
        }

        while (true) {
            LocalDate date = view.readDate();
            if (date == null) {  // Enter = 입력 취소
                return;
            }
            if (!date.isBefore(LocalDate.now())) { //오늘보다 이전이 아니면 (date.isAfter(오늘))의 참 로직은 오늘을 포함하게 됨)
                view.displayMessage("오늘 출결은 내일 입력하세요."); //막는다
                continue; //처음으로 돌아감
            }
            if (!classDayRepository.isClassDay(date)) {
                view.displayMessage("수업일이 아닙니다.");
                continue;
            }

            Attendance attendance = new Attendance();
            attendance.setMemberId(member.getMemberId());
            attendance.setAttendDate(date);

            if (view.readType() == AttendanceView.ABSENT) {
                attendance.setStatus(AttendanceStatus.ABSENT);    // 결석은 시각 없이 저장
            } else {
                LocalTime checkIn = view.readTime("입실 시각 (예: 09:05): ");
                LocalTime checkOut = view.readTime("퇴실 시각 (예: 18:25, 퇴실 전이면 Enter): ");

                if (checkIn == null) {
                    view.displayMessage("입실 시각을 입력해 주세요.");
                    continue;
                }
                if (checkOut == null) {
                    view.displayMessage("퇴실한 뒤 입력하세요.");
                    continue;
                }
                if (checkOut.isBefore(checkIn)) {
                    view.displayMessage("퇴실 시각은 입실 시각보다 빠를 수 없습니다.");
                    continue;
                }

                attendance.setCheckIn(checkIn);
                attendance.setCheckOut(checkOut);
                attendance.setStatus(rule.judge(checkIn, checkOut, course));
            }

            // 5a: 같은 날짜 기록이 이미 있으면 안내 후 다시 입력
            if (!attendanceRepository.save(attendance)) {
                view.displayMessage("이미 기록이 있습니다.");
                continue;
            }

            view.displayMessage(date.format(RESULT_DATE) + " 출결을 저장했습니다: "
                    + attendance.getStatus().getLabel());
            return;
        }
    }

    /*
     * 내 출결 리스크 보기 (L-08)
     * 흐름: 과정 조회 → 조회 기간 → 출결 개수 → 미입력 → 출석률 → 경고 계산 → 화면 출력
     * - member: 로그인한 회원 (Application이 MemberController에서 꺼내 넘긴다)
     * - 과정은 출결 입력(L-05)과 같은 findCourse()로 가져온다. v0.1은 과정이 1개라 과정 번호를 묻지 않는다
     */
    public void showMyRisk(Member member) {
        if (member == null) {
            view.displayMessage("로그인 후 이용해 주세요.");
            return;
        }

        // 1. 과정 조회. 과정이 없으면 여기서 끝냄
        Course course = courseRepository.findCourse();
        if (course == null) {
            view.displayMessage("과정 설정이 없습니다. 과정 설정을 먼저 해 주세요.");
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
            view.displayMessage("이번 달은 훈련 기간이 아닙니다.");
            return;
        }

        // 3. 출결 종류별 개수 → 기록 일수, 지각·조퇴·외출 환산 횟수 합계
        Map<AttendanceStatus, Integer> counts = attendanceRepository.countByStatus(member.getMemberId(), start, end);
        int recordDays = 0; // 기록 일수 = 모든 종류 개수의 합
        int partial = 0;    // 환산 횟수 합 = 개수 × 종류별 환산 횟수 (지각·조퇴는 2회, AttendanceStatus 참고)
        for (Map.Entry<AttendanceStatus, Integer> entry : counts.entrySet()) {
            recordDays += entry.getValue();
            partial += entry.getValue() * entry.getKey().getPartialCount();
        }

        // 기록이 하나도 없으면 계산하지 않는다 (PLAN L-08 예외 흐름 2a)
        if (recordDays == 0) {
            view.displayMessage("이번 달 기록이 없습니다 — 출결을 입력하세요");
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

        // 7. 화면 출력
        view.printRisk(start, end, classDays, recordDays, missing, counts,
                converted, rate, moreAbsences, morePartials, course);
    }
}
