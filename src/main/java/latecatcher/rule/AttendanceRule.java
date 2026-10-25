package latecatcher.rule;

import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

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

    // 출석률   = (기록 일수 − 공결 − 환산 결석) / (기록 일수 − 공결) × 100
    public OptionalDouble attendanceRate(int recordDays, int excused, int convertedAbsence) {

        int targetDays = (recordDays - excused); //출석률 계산의 분모
        if (targetDays <= 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((targetDays - convertedAbsence) * 100.0 / targetDays);
    }

    // 미입력 일수 = 기간의 수업일 수 − 기록 일수. 결석으로 세지 않고 화면에만 보여 준다 (PLAN 3절)
    public int missingDays(int classDays, int recordDays) {
        return classDays - recordDays;
    }

    // 기준일(baseDate)까지의 기록만 새 목록으로 돌려준다. 받은 records는 바꾸지 않는다 (PLAN 3절 계산 기준일)
    // - baseDate = 계산 기준일 = 어제. 기준일 당일은 포함하고, 그 뒤(오늘 이후)는 뺀다
    // - 오늘을 빼는 이유: 오늘은 아직 퇴실 전이라 퇴실 기록이 없다. judge 규칙(퇴실 없음 = 결석)대로 세면
    //   수업 중인 사람이 결석이 된다. 고용24도 "당일 출석결과는 익일 반영"이다
    // - 누가 넘기나: 계산을 부르는 쪽(L-08 내 출결 리스크의 Controller)이 LocalDate.now().minusDays(1)을 넘긴다
    // - 여기서 LocalDate.now()를 부르지 않는 이유: 테스트에서 날짜를 고정해야 언제 돌려도 같은 결과가 나온다
    // - 오늘 줄이 들어오는 경로: L-05 입력은 오늘을 막고(3b), v0.2 고용24 가져오기도 오늘 이후 줄은 가져오지 않는다.
    //   이 메서드는 그래도 섞여 들어온 줄을 계산 전에 거르는 마지막 안전장치다
    public List<Attendance> recordsUntil(List<Attendance> records, LocalDate baseDate) {
        List<Attendance> result = new ArrayList<>();

        for (Attendance attendance : records) {
            if (!attendance.getAttendDate().isAfter(baseDate)) {                                       // 3. 기준일보다 뒤가 아니면
                result.add(attendance);
            }
        }
        return result;
    }
}
