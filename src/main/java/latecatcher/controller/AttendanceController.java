package latecatcher.controller;

import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.ClassStatusRow;
import latecatcher.model.Course;
import latecatcher.model.Member;
import latecatcher.model.MonthlySummary;
import latecatcher.repository.AttendanceRepository;
import latecatcher.repository.ClassDayRepository;
import latecatcher.repository.CourseRepository;
import latecatcher.rule.AttendanceRisk;
import latecatcher.rule.AttendanceRule;
import latecatcher.view.AttendanceView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

// 출결 입력 흐름 (L-05). 날짜·구분·시각을 확인하고 AttendanceRule로 판정해 저장한다
// 입력과 출력은 AttendanceView가 하고, 판단·판정·저장은 여기서 한다 (나중에 메신저 입력 L-15도 같은 경로를 쓴다)
// L-08 내 출결 리스크. 조회(Repository)와 계산(AttendanceRule, AttendanceRisk)을 모아 화면(AttendanceView)에 넘긴다
// L-09 반 전체 현황. 월별 출석률 순위와 미달·경계 명단, 기록 없는 훈련생을 모아 화면에 넘긴다
public class AttendanceController {
    private static final DateTimeFormatter RESULT_DATE = DateTimeFormatter.ofPattern("MM/dd");
    // 경계 폭 (%p). 장려금 기준 이상, 기준 + 5%p 이하면 경계다 (이슈 #11 정할 것 4)
    private static final int BORDER_MARGIN = 5;

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

    /*
     * 반 전체 현황 (L-09, 이슈 #11)
     * 흐름: 과정 조회 → 월 입력 → 조회 기간 → 조회(뷰·HAVING·LEFT JOIN) → 출석률·미입력 → 순위 정렬 → 경계 분류 → 화면 출력
     * - 기준은 고정 80이 아니라 과정의 장려금 기준(allowance_threshold). 장려금은 월 출석률로 판정한다
     * - 미달 명단은 HAVING으로 가져오고, 순위·경계는 Java(AttendanceRule)로 계산한다 (이슈 #11 정할 것 1)
     * - 데이터는 바꾸지 않는다
     */
    public void showClassStatus() {
        // 1. 과정 조회. L-05·L-08과 같은 findCourse()를 써야 메뉴 1과 출석률이 같게 나온다
        Course course = courseRepository.findCourse();
        if (course == null) {
            view.displayMessage("과정 설정이 없습니다. 과정 설정을 먼저 해 주세요.");
            return;
        }
        int threshold = course.getAllowanceThreshold();

        // 2. 조회 월 입력. 훈련 기간 밖이면 안내 후 다시 묻는다 (예외 1a)
        YearMonth month;
        LocalDate start;
        LocalDate end;
        while (true) {
            month = view.readMonth();
            if (month == null) {  // Enter = 취소
                return;
            }

            // 조회 기간 = 그 달 1일 ~ (그 달 말일·어제·과정 종료일 중 가장 이른 날). 과정 시작일보다 앞이면 시작일부터
            // L-08과 같은 계산 기준일이다. 오늘은 퇴실 전이라 뺀다 (PLAN 3절)
            start = month.atDay(1);
            if (start.isBefore(course.getStartDate())) {
                start = course.getStartDate();
            }
            end = month.atEndOfMonth();
            LocalDate yesterday = LocalDate.now().minusDays(1);
            if (yesterday.isBefore(end)) {
                end = yesterday;
            }
            if (course.getEndDate().isBefore(end)) {
                end = course.getEndDate();
            }

            // 시작이 끝보다 뒤면 그 달에 계산할 날이 없다 (훈련 기간 밖이거나 아직 오지 않은 달)
            if (start.isAfter(end)) {
                view.displayMessage("훈련 기간이 아닙니다.");
                continue;
            }
            break;
        }

        // 3. 조회
        //    - 순위 대상: 그 달 기록이 1건 이상인 훈련생 (v_monthly_attendance)
        //    - 미달 명단: HAVING rate < 기준
        //    - 기록 없음: LEFT JOIN … IS NULL. 순위에서 조용히 빼지 않고 따로 보여 준다
        List<MonthlySummary> summaries = attendanceRepository.findMonthlySummaries(month);
        List<MonthlySummary> belowList =
                attendanceRepository.findBelowThreshold(month, course.getPartialPerAbsence(), threshold);
        List<Member> noRecordMembers = attendanceRepository.findMembersWithoutRecord(start, end);
        int classDays = classDayRepository.countRealClassDays(start, end); // 기간의 수업일 수 (휴일 제외)

        // 4. 사람마다 출석률·미입력 계산 (L-08과 같은 AttendanceRule 공식)
        List<ClassStatusRow> rows = new ArrayList<>();
        for (MonthlySummary summary : summaries) {
            int converted = rule.convertedAbsence(summary.getAbsentCnt(), summary.getPartialCnt(), course);
            OptionalDouble rate = rule.attendanceRate(summary.getRecordCnt(), summary.getExcusedCnt(), converted);
            int missing = rule.missingDays(classDays, summary.getRecordCnt());
            rows.add(new ClassStatusRow(0, summary, rate, missing, false, false)); // 순위·분류는 아래에서 정한다
        }

        // 5. 순위 정렬: 출석률 높은 순. 출석률이 같으면 회원 번호 순, 계산할 수 없는 사람(empty)은 맨 뒤
        //    - Double.compare(뒤, 앞)처럼 순서를 바꿔 넣으면 내림차순이 된다 (chap16 실습의 평점 내림차순과 같은 방식)
        //    - compare 결과가 0(출석률 같음)이면 회원 번호로 한 번 더 비교한다
        rows.sort((row1, row2) -> {
            int byRate = Double.compare(rateForSort(row2), rateForSort(row1));
            if (byRate != 0) {
                return byRate;
            }
            return Integer.compare(row1.getSummary().getMemberId(), row2.getSummary().getMemberId());
        });

        // 6. 순위 매기기 + 경계·미달 분류
        //    - 같은 출석률은 같은 순위, 다음 순위는 그 인원만큼 건너뛴다 (100, 100, 90 → 1, 1, 3)
        //    - 경계 = 기준 이상, 기준 + 5%p 이하 / 미달 = 기준 미만
        List<ClassStatusRow> ranked = new ArrayList<>();
        int rank = 0;
        double prevRate = -1;
        for (int i = 0; i < rows.size(); i++) {
            ClassStatusRow row = rows.get(i);
            if (row.getRate().isEmpty()) { // 공결만 있음 → 순위·분류 없이 그대로
                ranked.add(row);
                continue;
            }
            double rate = row.getRate().getAsDouble();
            if (rate != prevRate) {
                rank = i + 1;
                prevRate = rate;
            }
            boolean below = rate < threshold;
            boolean border = !below && rate <= threshold + BORDER_MARGIN;
            ranked.add(new ClassStatusRow(rank, row.getSummary(), row.getRate(), row.getMissingDays(), border, below));
        }

        // 7. 화면 출력
        view.printClassStatus(month, start, end, classDays, ranked, belowList, noRecordMembers, course);
    }

    // 정렬용 출석률. 계산할 수 없는 사람(공결만 있음, empty)은 -1로 두어 맨 뒤로 보낸다
    private double rateForSort(ClassStatusRow row) {
        if (row.getRate().isPresent()) {
            return row.getRate().getAsDouble();
        }
        return -1;
    }
}
