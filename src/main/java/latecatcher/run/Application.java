package latecatcher.run;

import latecatcher.controller.ClassDayController;
import latecatcher.controller.MemberController;
import latecatcher.controller.AttendanceController;
import latecatcher.view.AttendanceView;
import latecatcher.model.Course;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

// 사용자 입력과 메뉴 실행을 담당한다.
public class Application {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            MemberController controller = new MemberController();
            runStartMenu(scanner, controller);
        } catch (IllegalStateException e) {
            // DB 처리에 실패하면 JDBCTemplate과 저장소가 IllegalStateException으로 감싸서 여기까지 올린다.
            //
            // [변경 전] 고정 문구 두 줄만 출력하고 예외 객체 e는 쓰지 않았다.
            //  - 연결 실패, 드라이버 없음, 회원 저장 실패 등 무엇이 실패해도 화면에는 같은 문구가 나왔다.
            //  - JDBCTemplate과 저장소가 예외에 담아 보낸 메시지는 어디에도 출력되지 않고 버려졌다.
            // [변경 후] 메시지 두 가지를 함께 출력한다.
            //  - e.getMessage(): 무엇이 실패했는지. 예: "DB에 연결하지 못했습니다."
            //  - e.getCause().getMessage(): 왜 실패했는지. new IllegalStateException(메시지, e)의
            //    두 번째 인자로 넘긴 원래 예외의 메시지다. 예: MySQL의 "Access denied for user ..."
            //  - 원인 예외 없이 메시지만 넣어 던진 경우도 있으므로 getCause()가 null인지 먼저 확인한다.
            System.out.println("데이터베이스 처리 중 오류가 발생했습니다: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("원인: " + e.getCause().getMessage());
            }
            System.out.println("db.properties 설정과 schema.sql 실행 상태를 확인하세요.");
        } catch (NoSuchElementException e) {
            System.out.println("입력이 종료되어 프로그램을 종료합니다.");
        }
    }

    private static void runStartMenu(Scanner scanner, MemberController controller) {
        while (true) {
            printStartMenu();
            int choice = readInt(scanner, "메뉴 선택: ");

            switch (choice) {
                case 1:
                    signUp(scanner, controller);
                    break;
                case 2:
                    if (login(scanner, controller)
                            && runAttendanceTrackingMenu(scanner, controller)) {
                        return;
                    }
                    break;
                case 9:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("올바른 메뉴 번호를 입력하세요.");
            }
        }
    }

    private static void printStartMenu() {
        System.out.println();
        System.out.println("===== 출결 관리 시스템 =====");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("9. 프로그램 종료");
    }

    private static void signUp(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 회원가입 =====");
        System.out.print("이름: ");
        String name = scanner.nextLine().trim();
        int age = readInt(scanner, "나이: ");
        System.out.print("휴대폰 번호 (예: 010-1234-5678): ");
        String phone = scanner.nextLine().trim();

        controller.signUp(name, age, phone);
    }

    private static boolean login(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 로그인 =====");
        String phone = readPhone(scanner, controller);
        if (phone.isEmpty()) {
            return false;
        }
        return controller.login(phone);
    }

    private static boolean runAttendanceTrackingMenu(Scanner scanner, MemberController controller) {
        //출결 입력(L-05)·내 출결 리스크(L-08)·반 전체 현황(L-09)용. View는 이 메서드가 받은 Scanner를 함께 쓴다. (System.in에 Scanner를 하나만 둔다)
        AttendanceController attendanceController = new AttendanceController(new AttendanceView(scanner));

        while (true) {
            printMenu();
            int choice = readInt(scanner, "메뉴 선택: ");

            switch (choice) {
                // 1. 내 출결 리스크 보기
                case 1:
                    System.out.println("\n[내 출결 리스크 조회]");
                    attendanceController.showMyRisk(controller.getLoginMember());
                    break;

                // 2. 출결 입력
                case 2:
                    System.out.println("\n[출결 입력]");
                    attendanceController.inputAttendance(controller.getLoginMember());
                    break;

                // 3. 반 전체 현황
                case 3:
                    System.out.println("\n[반 전체 현황]");
                    attendanceController.showClassStatus();
                    break;

                // 4. 과정 설정
                case 4:
                    addCourse(scanner, controller);
                    break; // addCourse 완료 후 while 루프 시작 지점으로 돌아가 printMenu() 재출력

                // 5. 수업일 캘린더
                case 5:
                    ClassDayController classDayController = new ClassDayController();

                    System.out.println("\n[수업일 캘린더]");
                    int courseId = readInt(scanner, "과정 번호 입력: "); // 숫자가 아니면 다시 묻는다

                    classDayController.generateClassDays(courseId);  // 수업일 없으면 생성
                    classDayController.showCalendar(courseId);       // 캘린더 출력
                    break;

                // 6. 회원 탈퇴
                case 6:
                    if (withdraw(scanner, controller)) {
                        return false; // 탈퇴하면 로그인 정보가 없으므로 시작 메뉴로 복귀
                    }
                    break;

                // 7. 로그아웃 (로그인 정보를 지우고 시작 메뉴로 복귀)
                case 7:
                    controller.logout();
                    return false; // false를 반환하여 프로그램은 계속 돌고 시작 메뉴로 복귀

                // 9. 프로그램 종료
                case 9:
                    System.out.println("프로그램을 종료합니다.");
                    return true; // true를 반환하여 메인 메서드 레벨에서 프로그램 완전 종료

                default:
                    System.out.println("올바른 메뉴 번호를 입력하세요.");
                    break;
            }

        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== 과정 메뉴 =====");
        System.out.println("1. 내 출결 리스크 보기");
        System.out.println("2. 출결 입력");
        System.out.println("3. 반 전체 현황(월별 순위, 80% 미달자, 기록 없음)");
        System.out.println("4. 과정 설정");
        System.out.println("5. 수업일 캘린더");
        System.out.println("6. 회원 탈퇴");
        System.out.println("7. 로그아웃");
        System.out.println("9. 프로그램 종료");
    }

    private static void addCourse(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 과정 추가 =====");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. 강의명 입력
        System.out.print("강의 이름 (예: AI서비스 개발자): ");
        String courseName = scanner.nextLine().trim();

        // 2. 강의 시작일 / 종료일 입력 (형식 검증 및 반복)
        LocalDate startDate = readDate(scanner, "강의 시작일 (예: 2026-03-02): ", dateFormatter);
        LocalDate endDate = readDate(scanner, "강의 종료일 (예: 2026-08-31): ", dateFormatter);

        // 3. 수업 시작 / 종료 시간 입력 (형식 검증 및 반복)
        LocalTime startTime = readTime(scanner, "수업 시작 시간 (예: 09:00): ", timeFormatter);

        LocalTime endTime = readTime(scanner, "수업 종료 시간 (예: 18:00): ", timeFormatter);

        // 4. 출결 및 수료 기준 입력
        int partialPerAbsence = readInt(scanner, "지각·조퇴·외출 합산 결석 기준 횟수 (예: 3): ");
        while (partialPerAbsence < 1) {
            System.out.println("1 이상의 숫자를 입력하세요.");
            partialPerAbsence = readInt(scanner, "지각·조퇴·외출 합산 결석 기준 횟수 (예: 3): ");
        }

        // 숫자가 아니면 readInt가 다시 묻는다. 0~100 범위는 MemberController.addCourse가 확인한다
        int allowanceThreshold = readInt(scanner, "훈련장려금 지급 기준 출석률 (%) (예: 80): ");
        int completionThreshold = readInt(scanner, "과정 수료 기준 출석률 (%) (예: 80): ");

        // 5. Course 객체 생성 및 Controller 등록
        Course course = new Course(
                0,
                courseName,
                startDate,
                endDate,
                startTime,
                endTime,
                partialPerAbsence,
                240, // minAttendMinutes: 최소 수업 시간(분). 입력받을지는 L-03에서 정한다
                allowanceThreshold,
                completionThreshold
        );

        controller.addCourse(course);
    }

    // L-11 회원 탈퇴. Y를 입력해야 탈퇴하고, 그 밖의 입력은 취소한다
    // 출결 기록은 attendance_tbl의 FK ON DELETE CASCADE로 회원과 함께 지워진다
    private static boolean withdraw(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 회원 탈퇴 =====");
        System.out.println("탈퇴하면 회원 정보와 모든 출결 기록이 삭제됩니다.");
        System.out.print("정말 탈퇴하시겠습니까? (Y/N): ");
        String answer = scanner.nextLine().trim();

        if (!answer.equalsIgnoreCase("Y")) {
            System.out.println("탈퇴를 취소했습니다.");
            return false;
        }
        return controller.deleteMyAccount();
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력하세요.");
            }
        }
    }

    // 날짜 안전 입력 헬퍼 메서드
    private static LocalDate readDate(Scanner scanner, String prompt, DateTimeFormatter formatter) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("[오류] 잘못된 날짜 형식입니다. YYYY-MM-DD 형식(예: 2026-03-02)으로 다시 입력해 주세요.");
            }
        }
    }

    // 시간 안전 입력 헬퍼 메서드
    private static LocalTime readTime(Scanner scanner, String prompt, DateTimeFormatter formatter) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("[오류] 잘못된 시간 형식입니다. HH:mm 형식(예: 09:00)으로 다시 입력해 주세요.");
            }
        }
    }
    private static String readPhone(Scanner scanner, MemberController controller) {
        while (true) {
            System.out.print("휴대폰 번호 (예: 010-1234-5678, 취소: Enter): ");
            String phone = scanner.nextLine().trim();

            if (phone.isEmpty() || controller.isValidPhone(phone)) {
                return phone;
            }
            System.out.println("010-1234-5678 형식으로 입력해 주세요.");
        }
    }
}
