package latecatcher.view;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

// L-08 내 출결 리스크 화면 출력. 계산은 하지 않고 AttendanceController가 넘긴 값만 보여 준다 (PLAN 2절 사용 장면)
public class AttendanceView {

    // 조회 기간을 "10/01"처럼 짧게 보여 주기 위한 형식
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd");

    /*
     * 안내 메시지 출력
     * - controller에서 결과나 에러 메시지를 보여줄 때 사용
     */
    public void printMessage(String message) {
        System.out.println(message);
    }

    /*
     * 내 출결 리스크 출력
     * - 출력 예시
     *   ===== 10월 내 출결 (10/01 ~ 10/25) =====
     *   수업일 17일 · 기록 16일 (미입력 1일)
     *   출석 12  지각 2  조퇴 1  결석 1
     *   환산 결석 2일 → 출석률 87.5%
     *
     *   ⚠ 결석 2일 더 하면 장려금 기준(80%) 미달
     *   ⚠ 지각·조퇴 6번 더 하면 장려금 기준(80%) 미달
     *   미입력 1일 — 결석이었다면 결석으로 입력하세요
     */
    public void printRisk(LocalDate start, LocalDate end, int classDays, int recordDays, int missing,
                          Map<AttendanceStatus, Integer> counts, int converted, OptionalDouble rate,
                          OptionalInt moreAbsences, OptionalInt morePartials, Course course) {

        int threshold = course.getAllowanceThreshold(); // 장려금 기준 %. 고정 80이 아니라 과정 설정값

        // 1. 제목: 조회 시작일의 달 (오늘이 1일이면 지난달 이름이 나온다)
        System.out.println();
        System.out.println("===== " + start.getMonthValue() + "월 내 출결 ("
                + start.format(SHORT_DATE) + " ~ " + end.format(SHORT_DATE) + ") =====");

        // 2. 수업일·기록·미입력 일수
        System.out.println("수업일 " + classDays + "일 · 기록 " + recordDays + "일 (미입력 " + missing + "일)");

        // 3. 종류별 개수. 0인 종류는 생략한다
        //    counts는 EnumMap이라 AttendanceStatus에 적힌 순서(출석 → 지각 → … → 공결)대로 나온다
        StringBuilder line = new StringBuilder();
        for (Map.Entry<AttendanceStatus, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 0) {
                line.append(entry.getKey().getLabel()).append(" ").append(entry.getValue()).append("  ");
            }
        }
        System.out.println(line.toString().trim());

        // 4. 출석률. 공결만 있으면 분모가 0이라 계산하지 않고, 경고도 보여 주지 않는다
        if (rate.isEmpty()) {
            System.out.println("출석률: 계산할 기록 없음 (공결만 있음)");
            return;
        }
        System.out.printf("환산 결석 %d일 → 출석률 %.1f%%%n", converted, rate.getAsDouble());
        System.out.println();

        // 5. 경고
        //    - moreAbsences가 0이면 이미 기준 아래다 → 미달 안내 한 줄만
        //    - 그 밖에는 "몇 번 더 하면 미달"을 보여 준다. empty(계산 불가)면 그 줄은 생략
        if (moreAbsences.isPresent() && moreAbsences.getAsInt() == 0) {
            System.out.println("✗ 이미 장려금 기준(" + threshold + "%) 미달입니다");
        } else {
            if (moreAbsences.isPresent()) {
                System.out.println("⚠ 결석 " + moreAbsences.getAsInt() + "일 더 하면 장려금 기준(" + threshold + "%) 미달");
            }
            if (morePartials.isPresent()) {
                System.out.println("⚠ 지각·조퇴 " + morePartials.getAsInt() + "번 더 하면 장려금 기준(" + threshold + "%) 미달");
            }
        }

        // 6. 미입력 안내 (PLAN L-08 기본 흐름 5). 미입력이 없으면 생략
        if (missing > 0) {
            System.out.println("미입력 " + missing + "일 — 결석이었다면 결석으로 입력하세요");
        }
    }
}
