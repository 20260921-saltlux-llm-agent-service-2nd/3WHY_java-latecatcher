package latecatcher.rule;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceRuleTest {

    private final AttendanceRule rule = new AttendanceRule();   // ① 검사할 대상

    private Course course() {                                   // ② 테스트용 과정
        Course course = new Course();
        course.setStartTime(LocalTime.of(9, 20));
        course.setEndTime(LocalTime.of(18, 20));
        course.setPartialPerAbsence(3);
        return course;
    }

    // 테스트1 - 정각이면 지각도 조퇴도 아니다.
    @Test
    void 정각_입실_정각_퇴실은_출석() {
        assertEquals(AttendanceStatus.PRESENT,
            rule.judge(LocalTime.of(9,20), LocalTime.of(18,20), course()));
    }

    // 테스트2 - 지각이면서 조퇴면 지각(입실 기준)
    @Test
    void 지각이면서_조퇴는_지각() {
        assertEquals(AttendanceStatus.LATE,
                rule.judge(LocalTime.of(9,30), LocalTime.of(17,0), course()));
    }

    // judge — 1분이라도 늦으면 지각
    @Test
    void 시작_1분_뒤_입실은_지각() {
        assertEquals(AttendanceStatus.LATE,
                rule.judge(LocalTime.of(9, 21), LocalTime.of(18, 20), course()));
    }

    // judge — 정각에 들어와 일찍 나가면 조퇴
    @Test
    void 지각_없이_일찍_퇴실은_조퇴() {
        assertEquals(AttendanceStatus.EARLY_LEAVE,
                rule.judge(LocalTime.of(9, 20), LocalTime.of(17, 0), course()));
    }

    // judge — 퇴실 기록이 없으면 결석 (PLAN 3절)
    @Test
    void 퇴실_기록_없으면_결석() {
        assertEquals(AttendanceStatus.ABSENT,
                rule.judge(LocalTime.of(9, 20), null, course()));
    }

    // judge — 입실 기록이 없어도 결석
    @Test
    void 입실_기록_없으면_결석() {
        assertEquals(AttendanceStatus.ABSENT,
                rule.judge(null, LocalTime.of(18, 20), course()));
    }

    // isEarlyLeave — 종료 시각 전에 나가면 true
    @Test
    void 일찍_퇴실하면_isEarlyLeave는_true() {
        assertTrue(rule.isEarlyLeave(LocalTime.of(17, 0), course()));
    }

    // isEarlyLeave — 정각 퇴실은 조퇴가 아니다
    @Test
    void 정각_퇴실은_조퇴_아님() {
        assertFalse(rule.isEarlyLeave(LocalTime.of(18, 20), course()));
    }

    // isEarlyLeave — 퇴실 기록이 없으면 결석이다
    @Test
    void 퇴실_없으면_isEarlyLeave() {
        assertFalse(rule.isEarlyLeave(null, course()));
    }

    // convertedAbsence — 3회가 안 차면 결석으로 치지 않는다
    @Test
    void 지각_2회는_환산_결석_0일() {
        assertEquals(0, rule.convertedAbsence(0, 2, course()));
    }

    // convertedAbsence — 3회 = 결석 1일
    @Test
    void 지각_3회는_환산_결석_1일() {
        assertEquals(1, rule.convertedAbsence(0, 3, course()));
    }

    // convertedAbsence — 남는 횟수는 버린다 (5 / 3 = 1)
    @Test
    void 지각_5회는_환산_결석_1일() {
        assertEquals(1, rule.convertedAbsence(0, 5, course()));
    }

    // convertedAbsence — 결석은 그대로 더한다
    @Test
    void 결석_1일_지각_3회는_환산_결석_2일() {
        assertEquals(2, rule.convertedAbsence(1, 3, course()));
    }

    // convertedAbsence — 기준 횟수는 상수가 아니라 Course 값을 쓴다
    @Test
    void 기준이_2회면_지각_3회는_환산_결석_1일_4회는_2일() {
        Course course = course();
        course.setPartialPerAbsence(2);
        assertEquals(1, rule.convertedAbsence(0, 3, course));
        assertEquals(2, rule.convertedAbsence(0, 4, course));
    }

    // attendanceRate — 3단계 예시: (10 − 1 − 2) / (10 − 1) = 7 / 9
    @Test
    void 기록10_공결1_환산결석2는_77점8퍼센트() {
        OptionalDouble rate = rule.attendanceRate(10, 1, 2);
        assertEquals(77.78, rate.getAsDouble(), 0.01);
    }

    // attendanceRate — 80% 경계
    @Test
    void 기록10_환산결석2는_80퍼센트() {
        assertEquals(80.0, rule.attendanceRate(10, 0, 2).getAsDouble(), 0.001);
    }

    // attendanceRate — 80% 미만
    @Test
    void 기록14_환산결석3은_80퍼센트_미만() {
        assertTrue(rule.attendanceRate(14, 0, 3).getAsDouble() < 80.0);
    }

    // attendanceRate — 결석이 없으면 100%
    @Test
    void 환산결석이_없으면_100퍼센트() {
        assertEquals(100.0, rule.attendanceRate(10, 0, 0).getAsDouble(), 0.001);
    }

    // attendanceRate — 공결은 분모·분자 양쪽에서 빠진다: (10 − 2 − 2) / (10 − 2) = 6 / 8
    @Test
    void 공결은_분모와_분자에서_빠진다() {
        assertEquals(75.0, rule.attendanceRate(10, 2, 2).getAsDouble(), 0.001);
    }

    // attendanceRate — 기록이 0일이면 계산하지 않는다 (기록 없음)
    @Test
    void 기록_0일이면_기록_없음() {
        assertTrue(rule.attendanceRate(0, 0, 0).isEmpty());
    }

    // attendanceRate — 기록이 모두 공결이면 분모가 0이라 계산하지 않는다
    @Test
    void 모두_공결이면_기록_없음() {
        assertTrue(rule.attendanceRate(3, 3, 0).isEmpty());
    }

    // missingDays — 3단계 예시: 수업일 12일 중 10일 기록 → 미입력 2일
    @Test
    void 수업일12_기록10은_미입력_2일() {
        assertEquals(2, rule.missingDays(12, 10));
    }

    // missingDays — 모두 기록했으면 미입력 0일
    @Test
    void 모두_기록하면_미입력_0일() {
        assertEquals(0, rule.missingDays(12, 12));
    }

    // missingDays — 하나도 기록하지 않았으면 수업일 전부가 미입력
    @Test
    void 기록이_없으면_수업일_전부가_미입력() {
        assertEquals(12, rule.missingDays(12, 0));
    }
}
