package latecatcher.controller;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.ClassDay;
import latecatcher.model.Course;
import latecatcher.repository.ClassDayRepository;
import latecatcher.repository.CourseRepository;
import latecatcher.view.ClassDayView;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassDayController {

    // 사용할 repository, view 객체를 미리 만들어둠
    private final ClassDayRepository classDayRepository = new ClassDayRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final ClassDayView view = new ClassDayView();

    /*
     * 수업일 생성 (L-04)
     * 흐름: 과정 기간 조회 → 중복 확인 → 날짜 계산 → 저장
     */
    public void generateClassDays(int courseId) {

        // 1. 과정 기간 조회
        //    CourseRepository는 Connection을 매개변수로 받는 방식이라
        //    여기서 연결을 열고, 다 쓰면 닫음
        Connection con = JDBCTemplate.getConnection();
        Course course = courseRepository.selectCoursePeriod(con, courseId);
        JDBCTemplate.close(con);

        // 과정이 없으면 여기서 끝냄
        if (course == null) {
            view.printMessage("존재하지 않는 과정입니다.");
            return;
        }

        LocalDate start = course.getStartDate();
        LocalDate end = course.getEndDate();

        // 2. 중복 확인
        //    이미 이 기간에 수업일이 있으면 다시 만들지 않음
        if (classDayRepository.countClassDays(start, end) > 0) {
            view.printMessage("이미 수업일이 생성된 기간입니다.");
            return;
        }

        // 3. 날짜 계산
        //    시작일부터 종료일까지 하루씩 이동하면서 ClassDay 객체를 만듦
        List<ClassDay> days = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            // getDayOfWeek() : 그 날짜의 요일을 꺼냄
            DayOfWeek dow = date.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

            if (isWeekend) {
                // 주말 → 수업일 아님(false), 메모 "주말"
                days.add(new ClassDay(date, false, "주말"));
            } else {
                // 평일 → 수업일(true), 메모 없음
                days.add(new ClassDay(date, true, null));
            }
        }

        // 4. 저장
        int result = classDayRepository.insertClassDays(days);
        view.printMessage(result + "일의 수업일이 생성되었습니다.");
    }

    /*
     * 수업일 캘린더 조회 (L-04)
     * - 시작일~종료일 사이 수업일을 가져와서 view로 출력
     */
    public void showCalendar(LocalDate start, LocalDate end) {
        List<ClassDay> days = classDayRepository.selectClassDays(start, end);
        view.printCalendar(days);
    }
}