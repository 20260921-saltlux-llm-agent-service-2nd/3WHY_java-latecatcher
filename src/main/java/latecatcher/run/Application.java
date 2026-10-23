package latecatcher.run;

import latecatcher.controller.MemberController;

import java.util.NoSuchElementException;
import java.util.Scanner;

// 사용자 입력과 메뉴 실행을 담당한다. 이 파일은 수정하지 않는다.
public class Application {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            MemberController controller = new MemberController();
            runStartMenu(scanner, controller);
        } catch (IllegalStateException e) {
            System.out.println("데이터베이스 처리 중 오류가 발생했습니다.");
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
        System.out.println("준비중입니다.");

    }

    private static boolean login(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 로그인 =====");
        System.out.print("휴대폰 번호 (예: 010-1234-5678): ");
        String phone = scanner.nextLine().trim();
        System.out.println("준비중입니다.");
        return true;
    }

    private static boolean runAttendanceTrackingMenu(Scanner scanner, MemberController controller) {
        while (true) {
            printMenu();
            int choice = readInt(scanner, "메뉴 선택: ");

            switch (choice) {
                // 내 출결 리스크 보기
                case 1:

                    // 출결 입력
                case 2:

                    // 반 전체 현황
                case 3:

                    // 과정 설정
                case 4:
                    addCourse(scanner, controller);
                    // 수업일 캘린더
                case 5:

                    //회원 탈퇴
                case 6:

                    // 로그아웃
                case 8:


                    // 프로그램 종료
                case 9:
                    System.out.println("프로그램을 종료합니다.");
                    return true;

                default:
                    System.out.println("올바른 메뉴 번호를 입력하세요.");
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
        System.out.println("7. 로그 아웃");
        System.out.println("9. 프로그램 종료");
    }

    private static void addCourse(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 과정 추가 =====");
        System.out.print("강의 이름 (예: AI서비스 개발자): ");
        String courseName = scanner.nextLine().trim();

        controller.addCourse();
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
}
