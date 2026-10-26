package latecatcher.controller;

import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import latecatcher.model.Member;
import latecatcher.repository.AttendanceRepository;
import latecatcher.repository.ClassDayRepository;
import latecatcher.repository.CourseRepository;
import latecatcher.rule.AttendanceRule;
import latecatcher.view.AttendanceView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// 출결 입력 흐름 (L-05). 날짜·구분·시각을 확인하고 AttendanceRule로 판정해 저장한다
// 입력과 출력은 AttendanceView가 하고, 판단·판정·저장은 여기서 한다 (나중에 메신저 입력 L-15도 같은 경로를 쓴다)
public class AttendanceController {
    private static final DateTimeFormatter RESULT_DATE = DateTimeFormatter.ofPattern("MM/dd");

    private final AttendanceView view;
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private final ClassDayRepository classDayRepository = new ClassDayRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final AttendanceRule rule = new AttendanceRule();

    public AttendanceController(AttendanceView view) {
        this.view = view;
    }

    // 로그인한 회원의 출결 1건을 입력받아 저장한다. 3a·3b·4a·5a는 안내 후 날짜 입력으로 돌아간다 (PLAN 4절 L-05)
    public void inputAttendance(Member member) {
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
}
