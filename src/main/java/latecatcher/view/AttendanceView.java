package latecatcher.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// 출결 입력 화면. 날짜·구분·시각을 입력받아 넘기고 결과 메시지를 보여 준다 (L-05)
// 형식만 확인한다. 오늘 이후·수업일·퇴실 빈칸 같은 판단과 저장은 AttendanceController가 한다
public class AttendanceView {
    public static final int PRESENT = 1; // 출석
    public static final int ABSENT = 2; //결석
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

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
}
