package latecatcher.view;

import latecatcher.model.AttendanceStatus;
import latecatcher.model.ClassStatusRow;
import latecatcher.model.Course;
import latecatcher.model.Member;
import latecatcher.model.MonthlySummary;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.Collectors;

// 출결 입력 화면. 날짜·구분·시각을 입력받아 넘기고 결과 메시지를 보여 준다 (L-05)
// 형식만 확인한다. 오늘 이후·수업일·퇴실 빈칸 같은 판단과 저장은 AttendanceController가 한다
// L-08 내 출결 리스크 화면 출력. 계산은 하지 않고 AttendanceController가 넘긴 값만 보여 준다 (PLAN 2절 사용 장면)
// L-09 반 전체 현황. 조회 월을 입력받고, 순위표와 미달·경계·기록 없음 명단을 보여 준다
public class AttendanceView {
    public static final int PRESENT = 1; // 출석
    public static final int ABSENT = 2; //결석
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    // 조회 기간을 "10/01"처럼 짧게 보여 주기 위한 형식 (L-08)
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd");
    // 조회 월을 "2026-10"처럼 받기 위한 형식 (L-09)
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

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

    // 조회 월을 yyyy-MM 형식으로 받는다 (L-09). 형식이 틀리면 다시 묻는다. 비우고 Enter면 null(취소)
    // - "2026-13"처럼 없는 달도 형식 오류로 걸린다 (YearMonth.parse가 월 1~12만 받는다)
    // - 훈련 기간 밖인지는 여기서 보지 않는다. AttendanceController가 과정 기간으로 판단한다
    public YearMonth readMonth() {
        while (true) {
            System.out.print("조회 월 (예: 2026-10, 취소 : Enter): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return YearMonth.parse(input, MONTH_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("yyyy-MM 형식으로 입력해 주세요. (예: 2026-10)");
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

    /*
     * 반 전체 현황 출력 (L-09)
     * - 계산·정렬·분류는 AttendanceController가 끝냈다. 여기서는 받은 순서대로 보여 주기만 한다
     * - 출력 예시
     *   ===== 2026년 10월 반 전체 현황 (10/01 ~ 10/26) =====
     *   수업일 16일 · 장려금 기준 80%
     *
     *   순위  출석률   기록  미입력  구분      이름
     *     1   100.0%   13일    3일             RY
     *     2    83.3%   12일    4일  경계       YY
     *     3    75.0%   13일    3일  80% 미달   홍길동
     *
     *   80% 미달: 1명 (홍길동)
     *   경계(80% 이상 85% 이하): 1명 (YY 83.3%)
     *   기록 없음: 1명 (YI)
     */
    public void printClassStatus(YearMonth month, LocalDate start, LocalDate end, int classDays,
                                 List<ClassStatusRow> rows, List<MonthlySummary> belowList,
                                 List<Member> noRecordMembers, Course course) {

        int threshold = course.getAllowanceThreshold(); // 장려금 기준 %. 고정 80이 아니라 과정 설정값

        // 1. 제목과 기간
        System.out.println();
        System.out.println("===== " + month.getYear() + "년 " + month.getMonthValue() + "월 반 전체 현황 ("
                + start.format(SHORT_DATE) + " ~ " + end.format(SHORT_DATE) + ") =====");
        System.out.println("수업일 " + classDays + "일 · 장려금 기준 " + threshold + "%");
        System.out.println();

        // 2. 출석률 순위. 이름은 한글 폭이 달라 줄이 어긋나므로 맨 뒤에 둔다
        if (rows.isEmpty()) {
            System.out.println("이 달 기록이 있는 훈련생이 없습니다.");
        } else {
            System.out.println("순위  출석률   기록  미입력  구분      이름");
            for (ClassStatusRow row : rows) {
                MonthlySummary summary = row.getSummary();
                String rank = row.getRank() == 0 ? "-" : String.valueOf(row.getRank()); // 0 = 순위 없음 (공결만)
                String rate = row.getRate().isPresent()
                        ? String.format("%.1f%%", row.getRate().getAsDouble())
                        : "-";
                String mark = ""; // 구분 칸. 기준 이상이고 경계도 아니면 비워 둔다
                if (row.isBelow()) {
                    mark = threshold + "% 미달";
                } else if (row.isBorder()) {
                    mark = "경계";
                } else if (row.getRate().isEmpty()) {
                    mark = "공결만";
                }
                System.out.printf("%4s  %7s  %3d일  %3d일  %-8s  %s%n",
                        rank, rate, summary.getRecordCnt(), row.getMissingDays(), mark, summary.getName());
            }
        }
        System.out.println();

        // 3~5. 명단 세 줄. 모양은 PLAN L-09 기본 흐름 4의 "기록 없음: N명 (이름…)"에 맞춘다
        //   - 이름 목록은 스트림으로 만든다 (chap16): stream() → (filter) → map(이름) → collect(joining)
        //   - toList()로 만든 List를 그대로 출력하면 [YI]처럼 대괄호가 붙는다
        //     프로젝트에서 []는 [반 전체 현황] 같은 제목에만 쓰므로, joining(", ")으로 이름만 이어 붙이고 ( )로 감싼다

        // 3. 미달 명단 (HAVING으로 가져온 것). 순위표의 "미달" 표시(Java 계산)와 같은 사람이어야 한다
        String belowNames = belowList.stream()
                .map(MonthlySummary::getName)
                .collect(Collectors.joining(", "));
        System.out.println(threshold + "% 미달: " + belowList.size() + "명" + names(belowNames));

        // 4. 경계 명단 (Java 계산). 기준 이상, 기준 + 5%p 이하. 이름 뒤에 출석률을 붙인다 (예: YY 83.3%)
        List<ClassStatusRow> borderRows = rows.stream()
                .filter(ClassStatusRow::isBorder)
                .toList();
        String borderNames = borderRows.stream()
                .map(row -> row.getSummary().getName() + String.format(" %.1f%%", row.getRate().getAsDouble()))
                .collect(Collectors.joining(", "));
        System.out.println("경계(" + threshold + "% 이상 " + (threshold + 5) + "% 이하): "
                + borderRows.size() + "명" + names(borderNames));

        // 5. 기록 없음 (LEFT JOIN … IS NULL). 순위에서 조용히 빼지 않고 이름까지 보여 준다 (PLAN L-09 기본 흐름 4)
        String noRecordNames = noRecordMembers.stream()
                .map(Member::getName)
                .collect(Collectors.joining(", "));
        System.out.println("기록 없음: " + noRecordMembers.size() + "명" + names(noRecordNames));
    }

    // 이름 목록을 " (이름1, 이름2)"로 감싼다. 0명이면 괄호 없이 빈 문자열 → "기록 없음: 0명"
    private String names(String joinedNames) {
        if (joinedNames.isEmpty()) {
            return "";
        }
        return " (" + joinedNames + ")";
    }
}
