package latecatcher.rule;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;

import java.time.LocalTime;

// 출결 규칙. 상태 판정·환산, 결석·출석률·미입력 계산을 순수 Java로 한다.(PLAN3절) (L-05, L-08, L-09)
public class AttendanceRule {

    // PLAN 3절 판정 순서: 결석 -> 지각 -> 조퇴 -> 출석
    public AttendanceStatus judge(LocalTime checkIn, LocalTime checkOut, Course course) {

        if (checkIn == null || checkOut == null) { // 결석
            return AttendanceStatus.ABSENT;
        } else if (checkIn.isAfter(course.getStartTime())) { // 지각 먼저, 지각 다음에 조퇴를 하게 되면 지각으로 판정
            return AttendanceStatus.LATE;
        } else if (isEarlyLeave(checkOut, course)) { // 조퇴는 isEarlyLeave로 판단
            return AttendanceStatus.EARLY_LEAVE;
        } else { // 그 외의 경우 출석
            return AttendanceStatus.PRESENT;
        }
    }


    // 퇴실 시각이 있고 종료 시각 이전이면 true. 지각이면서 조퇴인 날 memo를 채울 때도 쓴다 (L-05)
    public boolean isEarlyLeave(LocalTime checkOut, Course course) {

        return checkOut != null && checkOut.isBefore(course.getEndTime());
    }

}
