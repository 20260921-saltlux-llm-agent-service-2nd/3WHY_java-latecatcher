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


    /* PLAN 3절 출석률 공식:
     - 환산 결석 = 결석 일수 + floor((지각 + 조퇴 + 외출) / 3)
     - 출석률   = (기록 일수 − 공결 − 환산 결석) / (기록 일수 − 공결) × 100
     - 기록 일수 = 훈련 시작일 ~ 어제 사이의 수업일 가운데 출결 기록이 있는 날   (오늘 포함하지 않음)
     - 미입력 일수 = 같은 기간의 수업일 − 기록 일수   (계산에 넣지 않고 화면에만 보여 준다)

     * (예시)
     * 훈련생 한 명이 수업일 12일 중 10일을 기록했고, 결석 1·지각 4·공결 1이 있는 경우
     *
     * 기록 일수   = 10                       (수업일 12일 중 출결 기록이 있는 날)
     * 미입력 일수 = 12 − 10 = 2              (기록 안 한 날. 계산에서는 빼고 화면에만 표시)
     * 환산 결석   = 결석 1 + floor(지각 4 / 3) = 1 + 1 = 2
     * 출석률     = (10 − 공결 1 − 환산 결석 2) / (10 − 공결 1) × 100
     *            = 7 / 9 × 100 = 77.8%       → 80% 미달
     *
     * !! 기록이 없는 날을 빼는 이유: 기록이 없는 날을 결석이 아니라 아직 입력하지 않은 날로 보여주기 위함.
     */

    // absent-결석 수, partial-지각·조퇴·외출 수, 과정 -> 환산 결석 일수
    public int convertedAbsence(int absent, int partial, Course course) {
        return absent + (partial / course.getPartialPerAbsence());
    }

}
