package latecatcher.view;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.Course;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

// 출결 입력 화면. 날짜·구분·시각을 입력받아 넘기고 결과 메시지를 보여 준다 (L-05)
// 형식만 확인한다. 오늘 이후·수업일·퇴실 빈칸 같은 판단과 저장은 AttendanceController가 한다
// L-08 내 출결 리스크 화면 출력. 계산은 하지 않고 AttendanceController가 넘긴 값만 보여 준다 (PLAN 2절 사용 장면)
public class AttendanceView {
    public static final int PRESENT = 1; // 출석
    public static final int ABSENT = 2; //결석
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    // 조회 기간을 "10/01"처럼 짧게 보여 주기 위한 형식 (L-08)
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd");

    private final Scanner scanner;

    // Scanner는 Application이 만든 것을 받아 쓴다. System.in에 Scanner를 두 개 만들면 입력이 섞인다
    public AttendanceView(Scanner scanner) {
        this.scanner = scanner;
    }

    // 날짜를 yyyy-MM-dd 형식으로 받는다. 형식이 틀리면 다시 묻는다. 비우고 Enter면 null(입력 취소)
    public LocalDate readDate() {
        while (true) {
            System.out.print("날짜 (예: 2026-10-06), 취소 : Enter): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()){
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("yyyy-MM-dd 형식으로 입력해 주세요. (예: 2026-10-06)");
            }
        }
    }

    // 구분을 1(출석) 또는 2(결석)로 받는다. 다른 값이면 다시 묻는다
    public int readType() {
        while (true) {
            System.out.print("구분 (1. 출석 / 2. 결석): ");
            String input = scanner.nextLine().trim();
            if (input.equals("1")) {
                return PRESENT;
            } else if (input.equals("2")) {
                return ABSENT;
            } else {
                System.out.println("1 또는 2를 입력해 주세요.");
            }
        }
    }

    // 시각을 HH:mm 형식으로 받는다. 비우고 Enter면 null (퇴실 전이면 비워 둔다 → Controller가 4a 안내)
    public LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) { return null; }
            try {
                return LocalTime.parse(input, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("HH:mm 형식으로 입력해 주세요. (예: 09:05)");
            }
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    /*
     * 내 출결 리스크 출력 (L-08)
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
