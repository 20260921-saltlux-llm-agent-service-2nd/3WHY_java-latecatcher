package latecatcher.rule;

import latecatcher.model.Attendance;
import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceRuleTest {

    private final AttendanceRule rule = new AttendanceRule();   // ① 검사할 대상

    private Course course() {                                   // ② 테스트용 과정
        Course course = new Course();
        course.setStartTime(LocalTime.of(9, 20));
        course.setEndTime(LocalTime.of(18, 20));
        course.setPartialPerAbsence(3);
        course.setMinAttendMinutes(240);   // 최소 수업 시간 4시간
        return course;
    }

    // ③ 테스트용 출결 기록: 날짜와 퇴실 시각만 정한다 (나머지는 이 계산에 쓰지 않음)
    private Attendance attendance(LocalDate date, LocalTime checkOut) {
        Attendance attendance = new Attendance();
        attendance.setAttendDate(date);
        attendance.setCheckOut(checkOut);
        return attendance;
    }

    // 테스트1 - 정각이면 지각도 조퇴도 아니다.
    @Test
    void 정각_입실_정각_퇴실은_출석() {
        assertEquals(AttendanceStatus.PRESENT,
            rule.judge(LocalTime.of(9,20), LocalTime.of(18,20), course()));
    }

    // 지각이면서 조퇴(4시간 이상 수업)는 지각·조퇴 (10/26 결정, 환산 2회)
    @Test
    void 지각이면서_조퇴는_지각_조퇴() {
        assertEquals(AttendanceStatus.LATE_EARLY_LEAVE,
                rule.judge(LocalTime.of(9, 30), LocalTime.of(17, 0), course()));
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

    // recordsUntil — 기준일(어제) 당일까지는 남는다
    @Test
    void 기준일_전날과_당일_기록은_남는다() {
        List<Attendance> records = List.of(
                attendance(LocalDate.of(2026, 10, 20), LocalTime.of(18, 20)),
                attendance(LocalDate.of(2026, 10, 21), LocalTime.of(18, 20)));

        assertEquals(2, rule.recordsUntil(records, LocalDate.of(2026, 10, 21)).size());
    }

    // recordsUntil — 퇴실 기록 없는 오늘 줄은 계산에서 빠진다 (PLAN 13절 성공 기준)
    @Test
    void 퇴실_없는_오늘_줄은_빠진다() {
        List<Attendance> records = List.of(
                attendance(LocalDate.of(2026, 10, 20), LocalTime.of(18, 20)),
                attendance(LocalDate.of(2026, 10, 21), LocalTime.of(18, 20)),
                attendance(LocalDate.of(2026, 10, 22), null));   // 오늘, 아직 퇴실 전

        List<Attendance> result = rule.recordsUntil(records, LocalDate.of(2026, 10, 21));

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 10, 21), result.get(1).getAttendDate());
    }

    // recordsUntil — 받은 목록은 그대로 두고 새 목록을 돌려준다
    @Test
    void 원본_목록은_바꾸지_않는다() {
        List<Attendance> records = List.of(
                attendance(LocalDate.of(2026, 10, 21), LocalTime.of(18, 20)),
                attendance(LocalDate.of(2026, 10, 22), null));

        rule.recordsUntil(records, LocalDate.of(2026, 10, 21));

        assertEquals(2, records.size());
    }

    // recordsUntil — 기록이 없으면 빈 목록
    @Test
    void 기록이_없으면_빈_목록() {
        assertTrue(rule.recordsUntil(List.of(), LocalDate.of(2026, 10, 21)).isEmpty());
    }
    // 최소 수업 시간 — 정시 입실, 정확히 4시간 뒤 퇴실은 4시간을 채워 조퇴
    @Test
    void 정시_입실_4시간_정각_퇴실은_조퇴() {
        assertEquals(AttendanceStatus.EARLY_LEAVE,
                rule.judge(LocalTime.of(9, 20), LocalTime.of(13, 20), course()));
    }

    // 최소 수업 시간 — 1분이라도 모자라면 결석
    @Test
    void 정시_입실_4시간_전_퇴실은_결석() {
        assertEquals(AttendanceStatus.ABSENT,
                rule.judge(LocalTime.of(9, 20), LocalTime.of(13, 19), course()));
    }

    // 최소 수업 시간 — 지각하면 입실 시각부터 잰다. 10:20~13:20은 3시간이라 결석
    @Test
    void 지각_후_3시간만_있으면_결석() {
        assertEquals(AttendanceStatus.ABSENT,
                rule.judge(LocalTime.of(10, 20), LocalTime.of(13, 20), course()));
    }

    // 최소 수업 시간 — 10:20~14:20은 4시간을 채웠고 종료 전 퇴실이라 지각·조퇴
    @Test
    void 지각_후_4시간_채우고_퇴실하면_지각_조퇴() {
        assertEquals(AttendanceStatus.LATE_EARLY_LEAVE,
                rule.judge(LocalTime.of(10, 20), LocalTime.of(14, 20), course()));
    }

    // 최소 수업 시간 — 12:20~18:20은 6시간이고 종료까지 있었으니 지각
    @Test
    void 지각_후_종료까지_있으면_지각() {
        assertEquals(AttendanceStatus.LATE,
                rule.judge(LocalTime.of(12, 20), LocalTime.of(18, 20), course()));
    }

    // 최소 수업 시간 — 일찍 온 시간(08:50~09:20)은 포함하지 않는다. 09:20~13:00은 3시간 40분이라 결석
    @Test
    void 일찍_온_시간은_수업_시간에_포함하지_않는다() {
        assertEquals(AttendanceStatus.ABSENT,
                rule.judge(LocalTime.of(8, 50), LocalTime.of(13, 0), course()));
    }

    // 최소 수업 시간 — 기준은 상수가 아니라 Course 값이다. 3시간이면 10:20~13:20도 인정
    @Test
    void 기준이_3시간이면_지각_후_3시간_퇴실은_지각_조퇴() {
        Course course = course();
        course.setMinAttendMinutes(180);
        assertEquals(AttendanceStatus.LATE_EARLY_LEAVE,
                rule.judge(LocalTime.of(10, 20), LocalTime.of(13, 20), course));
    }

    // 환산 횟수 — 지각·조퇴는 2회
    @Test
    void 지각_조퇴는_환산_횟수_2회() {
        assertEquals(2, AttendanceStatus.LATE_EARLY_LEAVE.getPartialCount());
    }

    // 환산 횟수 — 지각·조퇴·외출은 1회, 출석·결석·공결은 0회
    @Test
    void 상태별_환산_횟수() {
        assertEquals(1, AttendanceStatus.LATE.getPartialCount());
        assertEquals(1, AttendanceStatus.EARLY_LEAVE.getPartialCount());
        assertEquals(1, AttendanceStatus.OUTING.getPartialCount());
        assertEquals(0, AttendanceStatus.PRESENT.getPartialCount());
        assertEquals(0, AttendanceStatus.ABSENT.getPartialCount());
        assertEquals(0, AttendanceStatus.EXCUSED.getPartialCount());
    }
}
