package skeleton;

public class Question {

    /*
     * Q1. JDBC 기반 사용자별 OTT 구독 관리 프로그램 완성하기
     *
     * 이 프로젝트는 회원가입 후 휴대폰 번호로 로그인한 회원이 자신의 OTT 구독을
     * 추가, 조회, 연장, 삭제하고 OTT별 이용 플랜 이력을 확인하거나 회원 탈퇴하는 프로그램이다.
     * Member와 Membership은 완성된 모델로 제공하며 Application은 실행 메뉴로 제공한다.
     * 각 repository는 Properties.loadFromXML()로 mapper 패키지의 SQL을 직접 읽는다.
     * 모든 데이터베이스 접근 코드는 repository 패키지 안에 작성한다.
     *
     * [실행 전 준비]
     * 1. MySQL 관리자 계정에서 resources/db/schema.sql을 실행한다.
     * 2. 이어서 resources/db/insert.sql을 실행한다.
     * 3. resources/db.properties를 자신의 MySQL 접속 정보에 맞게 수정한다.
     *
     * [구현 순서]
     * 1. skeleton/mapper의 repository별 XML
     *    - member-query.xml의 회원 저장/조회/탈퇴 SQL
     *    - membership-query.xml의 구독 추가/조회/연장/삭제 SQL
     *    - 각 entry의 key는 repository에 제시된 이름을 유지
     * 2. repository/MemberRepository.java
     *    - 신규 회원 저장
     *    - 휴대폰 번호로 로그인할 회원 조회
     *    - 로그인 회원 탈퇴
     * 3. repository/MembershipRepository.java
     *    - 로그인 회원의 신규 OTT 구독 추가
     *    - 로그인 회원의 현재 구독 목록 조회
     *    - 연장/삭제 선택을 위한 전체 구독 기록 조회
     *    - 로그인 회원 본인의 구독 기간 연장
     *    - 로그인 회원 본인의 구독 기록 삭제
     *    - OTT별 이용했던 플랜 이름 목록 조회
     * 4. view/MemberView.java
     *    - 현재 구독 목록과 ID/상태가 포함된 전체 구독 기록 출력
     *    - OTT별 플랜 목록과 안내 메시지 출력
     * 5. controller/MemberController.java
     *    - 회원가입 처리
     *    - 신규 구독 입력값 검증 및 추가 처리
     *    - 연장/삭제 전에 선택 가능한 구독 기록 존재 여부 전달
     *    - 로그인 회원 상태 보관 및 repository/view 연결
     *    - 회원 탈퇴 처리 및 성공 시 로그인 상태 해제
     *    - 로그아웃 처리
     *
     * [사용 흐름]
     * - 연장과 삭제 메뉴는 전체 구독 기록을 먼저 보여준 뒤 구독 ID를 입력받는다.
     * - 선택할 구독이 없으면 ID를 입력받지 않고 메뉴로 돌아간다.
     * - ID 입력 단계에서 0을 입력하면 작업을 취소한다.
     * - 삭제는 y 확인을 받은 뒤 실행하며 8을 입력하면 로그아웃한다.
     * - 회원 탈퇴는 y 확인을 받은 뒤 실행하고, 성공하면 시작 메뉴로 돌아간다.
     * - 회원 탈퇴 시 외래 키의 ON DELETE CASCADE로 해당 회원의 구독 기록도 삭제된다.
     *
     * [제약 사항]
     * - 메서드명, 매개변수, 반환형은 변경하지 않는다.
     * - SQL은 Java 문자열로 작성하지 않고 repository별 XML 파일에 작성한다.
     * - repository 생성자에서 Properties.loadFromXML()로 XML을 읽는다.
     * - 각 메서드에서는 prop.getProperty(key)로 SQL을 가져온다.
     * - SQL은 Statement가 아닌 PreparedStatement로 실행한다.
     * - JDBC 자원은 try-with-resources로 닫는다.
     * - 날짜 데이터는 ResultSet.getTimestamp(...).toLocalDateTime()으로 변환한다.
     * - INSERT, UPDATE, DELETE의 성공 여부는 executeUpdate() 반환값으로 판단한다.
     * - 연장과 삭제 쿼리에는 membership_id와 member_id 조건을 모두 사용한다.
     * - 회원 탈퇴 쿼리에는 로그인 회원의 member_id를 조건으로 사용한다.
     * - Application은 선택 가능한 구독 기록을 먼저 출력한 뒤 ID를 입력받는다.
     * - SQL 예외를 무시하거나 단순 출력하지 말고 원인을 포함한 예외로 변환한다.
     * - TODO가 있는 임시 반환문은 실제 구현 코드로 교체한다.
     */
}
