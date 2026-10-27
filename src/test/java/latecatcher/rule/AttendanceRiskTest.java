package latecatcher.rule;

import latecatcher.model.Course;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceRiskTest {

    private final AttendanceRisk risk = new AttendanceRisk();   // ① 검사할 대상

    private Course course() {                                   // ② 테스트용 과정: 3회 = 결석 1일, 기준 80%
        Course course = new Course();
        course.setPartialPerAbsence(3);
        course.setAllowanceThreshold(80);
        return course;
    }

    // PLAN 2절 사용 장면: 기록 16일, 결석 1, 지각 2 + 조퇴 1 = 3회 → 87.5%
    // 결석 1일 더 81.3%, 2일 더 75.0% → 2일
    @Test
    void 출석률_87점5_에서_결석_2일_더면_80퍼센트_미달() {
        assertEquals(OptionalInt.of(2), risk.moreAbsencesToFail(16, 0, 1, 3, course()));
    }

    // 같은 조건에서 지각·조퇴 3회 더 81.3%, 6회 더 75.0% → 6회
    @Test
    void 출석률_87점5_에서_지각_6회_더면_80퍼센트_미달() {
        assertEquals(OptionalInt.of(6), risk.morePartialsToFail(16, 0, 1, 3, course()));
    }

    // AttendanceRule 주석 예시: 기록 10일, 공결 1, 결석 1, 지각 4 → 77.8%. 이미 미달이면 0
    @Test
    void 이미_미달이면_0() {
        assertEquals(OptionalInt.of(0), risk.moreAbsencesToFail(10, 1, 1, 4, course()));
        assertEquals(OptionalInt.of(0), risk.morePartialsToFail(10, 1, 1, 4, course()));
    }

    // 정확히 80%는 미달이 아니다: 기록 10일, 결석 2 → 80.0%, 1일 더 70.0% → 1
    @Test
    void 정확히_80퍼센트는_미달이_아니다() {
        assertEquals(OptionalInt.of(1), risk.moreAbsencesToFail(10, 0, 2, 0, course()));
    }

    // 기록이 없으면(분모 0) 계산하지 않는다
    @Test
    void 기록이_없으면_empty() {
        assertEquals(OptionalInt.empty(), risk.moreAbsencesToFail(0, 0, 0, 0, course()));
        assertEquals(OptionalInt.empty(), risk.morePartialsToFail(0, 0, 0, 0, course()));
    }

    // 기준이 0%면 떨어질 수 없다
    @Test
    void 기준이_0퍼센트면_empty() {
        Course course = course();
        course.setAllowanceThreshold(0);
        assertEquals(OptionalInt.empty(), risk.moreAbsencesToFail(16, 0, 1, 3, course));
    }
}
