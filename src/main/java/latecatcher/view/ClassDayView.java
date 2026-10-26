package latecatcher.view;

import latecatcher.model.ClassDay;

import java.util.List;

public class ClassDayView {

    /*
     * 안내 메시지 출력
     * - controller에서 결과나 에러 메시지를 보여줄 때 사용
     */
    public void printMessage(String message) {
        System.out.println(message);
    }

    /*
     * 수업일 캘린더 출력
     * - 날짜, 요일, 수업 여부, 메모를 한 줄씩 출력
     */
    public void printCalendar(List<ClassDay> days) {

        // 조회된 수업일이 없으면 안내 후 끝냄
        if (days.isEmpty()) {
            System.out.println("생성된 수업일이 없습니다.");
            return;
        }

        System.out.println("===== 수업일 캘린더 =====");

        for (ClassDay day : days) {
            // isClassDay() : boolean 필드 classDay의 getter (Lombok이 is로 만듦)
            String status = day.isClassDay() ? "수업" : "휴일";

            // note가 null이면 빈칸으로 출력
            String note = (day.getNote() == null) ? "" : day.getNote();

            System.out.println(day.getClassDate() + " ("
                    + day.getClassDate().getDayOfWeek() + ") "
                    + status + " " + note);
        }
    }
}