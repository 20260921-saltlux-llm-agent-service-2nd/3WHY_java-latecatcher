package latecatcher.rule;

import latecatcher.model.Course;

import java.util.OptionalDouble;
import java.util.OptionalInt;

// 출결 리스크 경고 계산. "결석 며칠 / 지각·조퇴 몇 번 더면 기준(80%) 아래"를 순수 Java로 구한다 (L-08, PLAN 2절 사용 장면)
// - 출석률·환산 결석 공식은 AttendanceRule을 그대로 쓰고, 이 클래스는 "몇 번 더"만 계산한다
// - 기록 일수는 늘리지 않고 고정한다. PLAN 2절 예시와 같은 방식이다
//   (예) 기록 16일, 환산 결석 2일 → 결석 1일 더 = 환산 결석 3일 → 13/16 = 81.3%
public class AttendanceRisk {

    private final AttendanceRule rule = new AttendanceRule();

    /* 결석을 며칠 더 하면 출석률이 기준 아래로 떨어지는지 구한다
     * - 0이면 이미 기준 아래다
     * - empty면 계산할 수 없다 (기록 일수 − 공결이 0 이하, 또는 기준이 0%라 떨어질 수 없음)
     *
     * (예시) PLAN 2절: 기록 16일, 결석 1, 지각·조퇴 3회, 3회 = 결석 1일, 기준 80%
     *   +0일 → 환산 결석 2일 → 87.5%
     *   +1일 → 환산 결석 3일 → 81.3%
     *   +2일 → 환산 결석 4일 → 75.0%  → 2 반환
     */
    public OptionalInt moreAbsencesToFail(int recordDays, int excused, int absent, int partial, Course course) {
        int targetDays = recordDays - excused; // 출석률 계산의 분모

        // 결석을 분모만큼 더하면 출석률이 0%가 되므로 그 안에서 반드시 끝난다
        for (int more = 0; more <= targetDays; more++) {
            int converted = rule.convertedAbsence(absent + more, partial, course);
            // 환산 결석이 분모보다 크면 출석률이 마이너스가 된다. 실제로는 0% 아래가 없으니 더 세지 않는다
            if (converted > targetDays) {
                break;
            }
            if (isBelow(recordDays, excused, converted, course)) {
                return OptionalInt.of(more);
            }
        }
        return OptionalInt.empty();
    }

    /* 지각·조퇴를 몇 번 더 하면 출석률이 기준 아래로 떨어지는지 구한다
     * - 지각·조퇴는 partialPerAbsence회가 모여야 결석 1일이 되므로, 1번 더 해도 출석률이 그대로일 수 있다
     * - 0이면 이미 기준 아래, empty면 계산할 수 없다 (moreAbsencesToFail과 같음)
     *
     * (예시) PLAN 2절: 같은 조건에서 지각·조퇴 3회 → 6회가 되면 환산 결석 1일이 늘어난다
     *   +3회 → 환산 결석 3일 → 81.3%
     *   +6회 → 환산 결석 4일 → 75.0%  → 6 반환
     */
    public OptionalInt morePartialsToFail(int recordDays, int excused, int absent, int partial, Course course) {
        int targetDays = recordDays - excused;
        int limit = targetDays * course.getPartialPerAbsence(); // 환산 결석이 분모만큼 되는 횟수 = 출석률 0%

        for (int more = 0; more <= limit; more++) {
            int converted = rule.convertedAbsence(absent, partial + more, course);
            // 환산 결석이 분모보다 크면 출석률이 마이너스가 된다. 실제로는 0% 아래가 없으니 더 세지 않는다
            if (converted > targetDays) {
                break;
            }
            if (isBelow(recordDays, excused, converted, course)) {
                return OptionalInt.of(more);
            }
        }
        return OptionalInt.empty();
    }

    // 출석률이 장려금 기준(allowanceThreshold, 기본 80%)보다 낮으면 true. 정확히 80%는 기준을 채운 것이다
    private boolean isBelow(int recordDays, int excused, int converted, Course course) {
        OptionalDouble rate = rule.attendanceRate(recordDays, excused, converted);
        return rate.isPresent() && rate.getAsDouble() < course.getAllowanceThreshold();
    }
}
