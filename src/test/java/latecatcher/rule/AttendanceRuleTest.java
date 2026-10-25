package latecatcher.rule;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

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

}