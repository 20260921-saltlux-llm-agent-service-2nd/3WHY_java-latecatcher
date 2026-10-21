# 지각 잡는 자바 (java-latecatcher) — 3WHY 팀 프로젝트 계획

> slx-ai-campus에서 Java → MySQL → JDBC를 배운 뒤 만드는 첫 팀 프로젝트. 팀 **3WHY** (3명).
> 수업에서 받은 **JDBC 구독관리 스켈레톤**(`skeleton` 패키지, OTT 구독 관리)을
> **훈련생 출결 리스크 매니저**로 변형한다.
> GitHub 협업(이슈 → 브랜치 → PR → 리뷰 → 머지)을 함께 연습한다. 팀 규칙: [CONVENTION.md](CONVENTION.md)

---

## 1. 한 줄 정의

**고용24 출결 규칙(지각·조퇴 환산, 출석률 80%)을 코드로 옮겨서, 우리 반 훈련생 각자가 "지금 몇 번 더 늦으면 장려금·수료가 위험한지"를 바로 알 수 있게 해 주는 콘솔 프로그램.**

---

## 2. 왜 이것인가

- 우리는 매일 고용24 앱으로 QR 출결을 찍는다. 출석률에 **훈련장려금과 수료**가 걸려 있다.
- 그런데 "지각 2번 + 결석 1번이면 이번 달 출석률이 몇 %지?"를 바로 알려 주는 화면은 없다. 규칙이 머릿속 암산이다.
- 고용24는 개인 출결을 외부로 내보내는 API가 없다 (확인함). 그래서 **데이터는 손으로 넣거나 고용24 화면의 표를 복사해 가져오고, 규칙은 코드가 계산**한다. 고용24에는 출결 파일 내려받기도 없다 (8절).
- 사용자는 우리 팀 3명에서 시작해 반 전체로 넓힐 수 있다. **만드는 동안 실제로 쓴다.**

### 사용 장면

```text
[로그인: 010-1234-5678]
===== 이영희 — 10월 출결 =====
수업일 22일 중 진행 16일
출석 12  지각 2  조퇴 1  결석 1
환산 결석: 1 + (2+1)/3 = 2일  →  출석률 87.5%

⚠ 지각/조퇴 1회 더 → 환산 결석 2일 (변화 없음, 다음 1회에 3일 = 81.3%)
⚠ 결석 1일 더      → 환산 결석 3일 = 81.3%
⚠ 결석 2일 더      → 75.0%  ✗ 장려금 기준(80%) 미달
```

---

## 3. 출결 규칙 (코드의 심장)

아래는 국민내일배움카드 과정에서 일반적으로 적용되는 규칙이다. **정확한 수치는 훈련기관 안내문으로 확정하고 이 표를 고친다.** 코드는 이 값들을 상수가 아니라 `course_tbl` 설정값으로 읽는다.

| 규칙 | 기본값 (확인 필요) | 설정 컬럼 |
| --- | --- | --- |
| 수업 시작 / 종료 시각 | 09:00 / 18:00 | `start_time`, `end_time` |
| 지각 | 시작 시각 이후 입실 | — |
| 조퇴 | 종료 시각 이전 퇴실 | — |
| 퇴실 기록 없음 | **결석** (고용24 판정과 같음) | — |
| 계산 기준일 | **어제까지.** 오늘은 계산에서 뺀다 | — |
| 지각·조퇴·외출 환산 | **3회 = 결석 1일** (`floor(count / 3)`) | `partial_per_absence` = 3 |
| 단위기간 | 훈련 시작일 기준 1개월 | `unit_period_days` = 30 또는 월 단위 |
| 장려금 기준 출석률 | 단위기간 **80% 이상** | `allowance_threshold` = 80 |
| 수료 기준 출석률 | 전체 **80% 이상** | `completion_threshold` = 80 |
| 공결(인정 결석) | 출석률 분모·분자 모두 제외 | 상태 `EXCUSED` |

출석률 공식:

```text
환산 결석 = 결석 일수 + floor((지각 + 조퇴 + 외출) / 3)
출석률   = (진행 수업일 − 공결 − 환산 결석) / (진행 수업일 − 공결) × 100
진행 수업일 = 훈련 시작일 ~ 어제 사이의 수업일   (오늘 포함하지 않음)
```

**오늘을 빼는 이유:** 퇴실 기록이 없으면 결석이다. 그런데 오늘은 아직 퇴실하지 않았어도 퇴실 기록이 없으니, 오늘을 넣으면 수업 중인 사람도 결석으로 계산된다. 고용24도 "당일 출석결과는 익일 반영"이다. 그래서 오늘 줄은 DB에 있어도 출석률·환산 결석·경고 계산에 넣지 않는다. 기준일은 `LocalDate.now().minusDays(1)`로 정하고, `AttendanceRule`에는 인자로 넘겨 테스트에서 날짜를 고정할 수 있게 한다.

이 계산은 **순수 Java(`AttendanceRule`)** 에 둔다. SQL은 개수를 세고, Java가 규칙을 적용한다. 그래야 JUnit으로 규칙만 따로 테스트할 수 있다.

---

## 4. 기능

스켈레톤 기능 목록(F-01 ~ F-11)과 같은 형식으로 정리한다. 번호는 스켈레톤 이슈와 헷갈리지 않게 `L-`를 붙인다.
**스켈레톤** 컬럼은 대응하는 스켈레톤 기능과 활용 방식이다. 클래스 단위 변환은 [6절](#6-스켈레톤--지각-잡는-자바-변환표)을 본다.
**담당**은 [11절](#11-팀-구성과-역할-분담-3명)의 A·B·C이다.

### v0.1 — 핵심 (1. 규칙 + 2. 출결 가져오기)

| 번호 | 기능명 | 영문 이름(브랜치) | 입력 | 핵심 동작 | 스켈레톤 | 담당 |
| --- | --- | --- | --- | --- | --- | --- |
| L-01 | 회원가입 | `sign-up` | 이름, 나이, 휴대폰 번호 | 빈 값과 나이를 검사하고, 휴대폰 번호 중복 확인 후 훈련생을 등록한다. | F-01 그대로 | A |
| L-02 | 로그인 | `phone-login` | 휴대폰 번호 | 일치하는 훈련생을 조회하여 로그인 상태로 보관한다. | F-02 그대로 | A |
| L-03 | 과정 설정 | `course-setting` | 과정명, 훈련 시작·종료일, 수업 시작·종료 시각, 규칙 수치 | `course_tbl`에 과정 1행을 저장하거나 수정한다. 규칙 수치는 3절의 설정 컬럼이다. | 신규 | C |
| L-04 | 수업일 캘린더 | `class-calendar` | (생성) 없음 / (휴일 등록) 날짜, 메모 | 훈련 기간의 평일을 `class_day_tbl`에 생성한다. 공휴일·휴강일은 수동으로 수업일에서 뺀다. | 신규 | C |
| L-05 | 출결 입력 (수동) | `attendance-checkin` | 날짜, 입실 시각, 퇴실 시각 | 수업일인지 확인하고, 과정의 시작·종료 시각으로 상태(출석/지각/조퇴)를 판정해 저장한다. 같은 날짜 중복 입력은 알려 준다. | F-04 변형: 구독 추가 → 출결 추가. 만료일 계산 → 상태 판정 | B |
| L-06 | 출결 가져오기 | `attendance-import` | 붙여 넣은 텍스트 파일 경로, 저장 확인(y/n) | 고용24 출결 화면에서 복사해 저장한 표를 읽어 미리보기를 보여 주고, 확인을 받으면 한 트랜잭션으로 저장한다. **한 줄이라도 실패하면 전체 취소**한다. | 신규 | A |
| L-07 | 결석 일괄 마감 | `absence-close` | 없음 | 지난 수업일 중 출결 기록이 없는 훈련생을 `ABSENT`로 한 트랜잭션에서 일괄 저장한다. | 신규 | B |
| L-08 | 내 출결 리스크 | `my-risk` | 없음 (이번 달) | 로그인 훈련생의 월별 출석률과 환산 결석을 계산하고, 몇 번 더 늦으면 80% 아래로 떨어지는지 경고한다. 2절 사용 장면 화면이다. | F-03 변형: 현재 구독 조회 → 이번 달 출결 집계 | C |
| L-09 | 반 전체 현황 | `class-status` | 조회 월 | 전체 훈련생의 월별 출석률 순위와 **80% 미달·경계(85% 이하) 명단**을 보여 준다. | 신규 | C |
| L-10 | 로그아웃 | `logout` | 없음 | 로그인 정보를 제거하고 시작 메뉴로 돌아간다. | F-09 그대로 | A |
| L-11 | 회원 탈퇴 | `withdraw` | 탈퇴 확인 | 훈련생의 모든 출결 기록과 회원 정보를 삭제한 후 로그인 상태를 해제한다. | F-10 변형: 구독 기록 → 출결 기록 | A |
| L-12 | 프로그램 종료 | `exit` | 메뉴 번호 9 | 시작 메뉴 또는 출결 메뉴에서 프로그램을 종료한다. | F-11 변형: 구독 관리 메뉴 → 출결 메뉴 | C |

### v0.2 — 확장 (3. 외부 데이터)

| 번호 | 기능명 | 영문 이름(브랜치) | 입력 | 핵심 동작 | 스켈레톤 | 담당 |
| --- | --- | --- | --- | --- | --- | --- |
| L-13 | 공휴일 API | `holiday-api` | 연도 | 공공데이터포털 특일정보 API로 `class_day_tbl`의 공휴일을 자동 반영한다. (`HttpClient` + JSON) | 신규 | v0.1 후 이슈로 나눔 |
| L-14 | 텔레그램 봇 | `telegram-bot` | "도착" 메시지, `/내출결` | "도착" 메시지로 입실을 기록하고, 08:50 미체크 알림을 보내고, 출결을 조회한다. | 신규 | v0.1 후 이슈로 나눔 |

### 스켈레톤에서 가져오지 않는 기능

| 스켈레톤 | 이유 |
| --- | --- |
| F-05 전체 구독 기록 조회 | 일자별 출결 목록은 v0.1에 두지 않는다. L-08이 월 집계를 보여 준다. 필요해지면 L-08에 일자 목록을 붙인다. |
| F-06 구독 기간 연장 | 출결에는 연장 개념이 없다. (`extendMembership` 삭제, 6절) |
| F-07 구독 기록 삭제 | 잘못 넣은 출결의 수정·삭제는 v0.1 범위 밖이다. 같은 날짜 중복은 UNIQUE 제약으로 막는다(L-05). |
| F-08 OTT별 이용 플랜 조회 | 대응하는 기능이 없다. 그룹으로 묶어 보여 주는 연습은 L-09가 맡는다. |

### 하지 않는 것

- 고용24 로그인 자동화·스크래핑 (불가능하고 약관 위반 소지)
- 웹/GUI, 여러 과정 동시 관리, 커넥션 풀, 프레임워크
- 반 친구 데이터 무단 입력 — **본인 동의 + 본인 입력**만

---

## 5. 배운 것 → 이 프로젝트에서 실습하는 것

이 표가 이 프로젝트의 학습 목표다. PR을 올릴 때 "어느 항목을 실습했는지" 본문에 적는다.

### 5.1 Java (`01_Java/`)

| 배운 것 | 출처 | 이 프로젝트에서 |
| --- | --- | --- |
| 5층 구조 model / repository / view / controller / run | `chap99-Java-Application`, 스켈레톤 | 그대로 유지. 새 기능도 같은 층 구조로 |
| enum | `chap14_enum` | `AttendanceStatus` — 한글 라벨, "환산 대상인가" 플래그를 enum 필드로 |
| 인터페이스·다형성 | `chap09-polymorphism` | `AttendanceImporter` 인터페이스 — v0.1 `Work24Importer`, v0.2 `TelegramImporter`. 가져오는 곳이 달라도 저장 코드는 하나 |
| 상속·예외 | `chap07-inheritance` | `ImportException extends RuntimeException` — 몇 번째 줄이 왜 실패했는지 담는다 |
| 컬렉션·제네릭 | `chap11-collections`, `chap10-Generics` | `Map<LocalDate, Attendance>` 로 날짜별 기록, `List<MonthlySummary>` |
| 람다·스트림 | `chap15-lambda`, `chap16-stream` | 붙여 넣은 줄 → `dropWhile(헤더 전)` → `filter(빈 줄 제외)` → `map(파싱)` → `toList()`; 랭킹 `sorted(comparing(...))`; 경고 계산 |
| java.time | `chap09-api` | `LocalDate`/`LocalTime`/`YearMonth`, 평일 생성 (`DayOfWeek`), 월 경계 계산 |
| **파일 읽기** | **새로 배움** (`Properties.load(FileReader)`는 이미 씀) | `Files.readAllLines(Path)` 또는 `BufferedReader` 로 붙여 넣은 텍스트 파일 읽기. 작은 확장이지만 처음 |
| 스레드 | `chap17-thread` | v0.2 텔레그램 폴링 루프 (`ScheduledExecutorService`) |

### 5.2 MySQL (`02_Database/`)

| 배운 것 | 출처 | 이 프로젝트에서 |
| --- | --- | --- |
| DDL + 제약조건 (PK/FK/UNIQUE/CHECK/DEFAULT/ENUM) | `04_DDL/01_DDL`, `02.Constraint` | `attendance_tbl`의 `UNIQUE(member_id, attend_date)` = "하루 한 기록", `CHECK (check_out >= check_in)`, `ENUM` 상태 |
| JOIN (INNER / LEFT) | `01_DQL/07_JOIN` | **LEFT JOIN + IS NULL** 로 "수업일인데 기록 없는 훈련생" = 결석 마감 대상 |
| GROUP BY / HAVING | `01_DQL/08_GroupByHaving` | 훈련생·월별 상태 개수; **HAVING** 으로 80% 미달자만 |
| 내장 함수 | `01_DQL/06_Built_In_Function` | `SUM(CASE WHEN status='LATE' THEN 1 END)`, `DATE_FORMAT(attend_date,'%Y-%m')`, `COUNT`, `FLOOR` |
| 서브쿼리 / CTE | `01_DQL/09_SubQuery` | 월별 집계를 CTE로 만들고 바깥에서 출석률 계산 |
| VIEW | `04_DDL/03_View` | `v_monthly_attendance` — 월별 집계를 뷰로 두고 Java는 뷰만 SELECT |
| INDEX | `04_DDL/04_Index` | `attendance_tbl(attend_date)` 인덱스. 월별 조회 전후 `EXPLAIN` 비교해 보기 |
| DML — INSERT/UPDATE/DELETE/**REPLACE** | `02_DML` | 출결 **재가져오기**는 `REPLACE INTO` 로 같은 날짜 기록을 덮어쓴다 (UNIQUE 덕분에 가능) |
| **트랜잭션** | `03_Transaction` (SQL로만 배움) | **JDBC에서 처음 쓴다** — 아래 5.3 |

### 5.3 JDBC (`03_jdbc/`)

| 배운 것 | 출처 | 이 프로젝트에서 |
| --- | --- | --- |
| `JDBCTemplate.getConnection()` + `db.properties` | `05-JDBC`, 스켈레톤 | 그대로. `db.properties`는 gitignore (이미 설정됨) |
| SQL을 `mapper/*.xml`에 두고 `Properties.loadFromXML` | `06-JDBC-Exercise`, 스켈레톤 | `attendance-query.xml`, `course-query.xml` 추가 |
| `PreparedStatement` + `?` 바인딩 | `05-JDBC/section2/c_preparedStatement` | 모든 쿼리 |
| `ResultSet` → DTO 매핑 | 스켈레톤 `findByPhone` | `getObject(col, LocalDate.class)` / `LocalTime.class` — 스켈레톤의 `getTimestamp().toLocalDateTime()`에서 한 단계 확장 |
| try-with-resources, `SQLException` → `IllegalStateException` | 스켈레톤 규칙 | 그대로 유지 |
| `executeUpdate()` 반환값으로 성공 판정 | 스켈레톤 | 그대로 |
| **트랜잭션 (`setAutoCommit(false)` / `commit` / `rollback`)** | **새로 배움** — SQL `START TRANSACTION`을 JDBC로 | ① 출결 가져오기: N행 INSERT + `import_log_tbl` 1행, 하나라도 실패하면 **전부 롤백** ② 결석 일괄 마감 ③ 비교용: 회원 탈퇴는 트랜잭션 없이 **FK CASCADE**가 처리 — 둘의 차이를 PR에 적는다 |
| **`SQLIntegrityConstraintViolationException` 구분** | 새로 배움 | 같은 날 중복 입력 → UNIQUE 위반을 잡아 "이미 기록이 있습니다"로 안내 |
| **`addBatch()` / `executeBatch()`** | 새로 배움 (선택) | 가져올 줄이 100행 이상이면 배치 실행. 먼저 반복 `executeUpdate`로 만들고, 시간 비교 뒤 배치로 바꿔 보기 |

> 트랜잭션은 이 프로젝트에서 **가장 중요하게 실습하는 항목**이다. 출결 가져오기 PR에는 반드시 "중간 줄을 고의로 깨뜨려 롤백되는 것을 확인한" 기록을 넣는다.

---

## 6. 스켈레톤 → 지각 잡는 자바 변환표

현재 레포 `src/main/java/skeleton/` 기준. 패키지 이름은 0단계에서 `com.latecatcher.app`으로 바꾼다. 폴더 구조는 [9절](#9-프로젝트-구조).

| 스켈레톤 (OTT 구독관리) | 지각 잡는 자바 | 바뀌는 것 |
| --- | --- | --- |
| `model.Member` (memberId, name, age, phone) | `model.Member` **그대로** | 훈련생. age는 두어도 무해 |
| `model.Membership` (ottType, planName, enrollDate, expiryDate, active) | `model.Attendance` (attendanceId, memberId, attendDate, checkIn, checkOut, status, memo, source) | 구독 1건 → 출결 1건. `LocalDateTime` 2개 → `LocalDate` + `LocalTime` 2개 |
| — | `model.AttendanceStatus` (enum) | `PRESENT, LATE, EARLY_LEAVE, OUTING, ABSENT, EXCUSED` |
| — | `model.Course` | 과정 설정 1행 |
| — | `model.MonthlySummary` | 월별 집계 결과 (뷰 1행) |
| `repository.MemberRepository` (save / findByPhone / deleteById) | **그대로** | 이미 구현 중 |
| `repository.MembershipRepository` | `repository.AttendanceRepository` | `addMembership` → `save`, `findAllMembershipsByMemberId` → `findByMemberAndMonth`, `extendMembership` → 삭제, + `saveAll(List, Connection)` (트랜잭션용), `findMissingOnDate` (LEFT JOIN) |
| — | `repository.CourseRepository`, `repository.ClassDayRepository` | 설정·캘린더 |
| — | `service` 계층? | **도입하지 않는다.** 규칙은 `rule.AttendanceRule` (순수 Java 클래스)에, 흐름은 Controller에. 아직 안 배운 계층을 만들지 않는다 |
| — | `importer.AttendanceImporter` (interface), `importer.Work24Importer` | 파일 → `List<Attendance>` |
| `controller.MemberController` (로그인 상태 보관) | `controller.MemberController` + `controller.AttendanceController` | 로그인 흐름 유지. 출결 메뉴는 새 컨트롤러 |
| `view.MemberView` | `view.MemberView` + `view.AttendanceView` | 리스크 화면 출력 |
| `run.Application` (회원가입/로그인 → 구독 메뉴) | `run.Application` | 구독 메뉴 → 출결 메뉴로 교체 |
| `mapper/membership-query.xml` | `mapper/attendance-query.xml`, `course-query.xml`, `classday-query.xml` | |
| `resources/db/schema.sql` (smsdb) | `resources/db/schema.sql` (**latedb**) + `insert.sql` (팀 3명 + 10월 샘플) | 아래 7절 |

---

## 7. 데이터 설계

스켈레톤 스키마 스타일(`_tbl` 접미사, 이름 붙인 제약조건) 유지.

```sql
CREATE DATABASE IF NOT EXISTS latedb DEFAULT CHARACTER SET utf8mb4;
USE latedb;

CREATE TABLE member_tbl (                           -- 스켈레톤 그대로
    member_id INT AUTO_INCREMENT,
    name      VARCHAR(30) NOT NULL,
    age       INT         NOT NULL,
    phone     VARCHAR(20) NOT NULL,
    CONSTRAINT pk_member_tbl PRIMARY KEY (member_id),
    CONSTRAINT uq_member_tbl_phone UNIQUE (phone)
);

CREATE TABLE course_tbl (                           -- 과정 설정, 1행만 사용
    course_id             INT AUTO_INCREMENT,
    name                  VARCHAR(100) NOT NULL,
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    start_time            TIME NOT NULL DEFAULT '09:00:00',
    end_time              TIME NOT NULL DEFAULT '18:00:00',
    partial_per_absence   INT  NOT NULL DEFAULT 3,   -- 지각·조퇴·외출 n회 = 결석 1일
    allowance_threshold   INT  NOT NULL DEFAULT 80,  -- 장려금 기준 %
    completion_threshold  INT  NOT NULL DEFAULT 80,  -- 수료 기준 %
    CONSTRAINT pk_course_tbl PRIMARY KEY (course_id),
    CONSTRAINT ck_course_tbl_period CHECK (end_date >= start_date)
);

CREATE TABLE class_day_tbl (                        -- 수업일 캘린더
    class_date   DATE NOT NULL,
    is_class_day BOOLEAN NOT NULL DEFAULT TRUE,     -- FALSE = 공휴일/휴강
    note         VARCHAR(50),                       -- '개천절', '휴강'
    CONSTRAINT pk_class_day_tbl PRIMARY KEY (class_date)
);

CREATE TABLE attendance_tbl (                       -- 출결 1건 = 훈련생 1명의 하루
    attendance_id INT AUTO_INCREMENT,
    member_id     INT  NOT NULL,
    attend_date   DATE NOT NULL,
    check_in      TIME,
    check_out     TIME,
    status        ENUM('PRESENT','LATE','EARLY_LEAVE','OUTING','ABSENT','EXCUSED') NOT NULL,
    memo          VARCHAR(200),
    source        ENUM('MANUAL','WORK24','TELEGRAM') NOT NULL DEFAULT 'MANUAL',
    CONSTRAINT pk_attendance_tbl PRIMARY KEY (attendance_id),
    CONSTRAINT fk_attendance_tbl_member_tbl FOREIGN KEY (member_id)
        REFERENCES member_tbl (member_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_tbl_class_day_tbl FOREIGN KEY (attend_date)
        REFERENCES class_day_tbl (class_date),
    CONSTRAINT uq_attendance_tbl_member_date UNIQUE (member_id, attend_date),
    CONSTRAINT ck_attendance_tbl_time CHECK (check_out IS NULL OR check_in IS NULL OR check_out >= check_in)
);

CREATE INDEX idx_attendance_tbl_date ON attendance_tbl (attend_date);

CREATE TABLE import_log_tbl (                       -- 출결 가져오기 기록 (트랜잭션의 일부)
    import_id   INT AUTO_INCREMENT,
    file_name   VARCHAR(255) NOT NULL,
    row_count   INT NOT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    imported_by INT,
    CONSTRAINT pk_import_log_tbl PRIMARY KEY (import_id),
    CONSTRAINT fk_import_log_tbl_member_tbl FOREIGN KEY (imported_by) REFERENCES member_tbl (member_id)
);

-- 월별 집계 뷰: SQL은 세기만 하고, 출석률 계산은 Java(AttendanceRule)가 한다
CREATE OR REPLACE VIEW v_monthly_attendance AS
SELECT m.member_id,
       m.name,
       DATE_FORMAT(a.attend_date, '%Y-%m')                       AS ym,
       SUM(a.status = 'PRESENT')                                  AS present_cnt,
       SUM(a.status = 'LATE')                                     AS late_cnt,
       SUM(a.status = 'EARLY_LEAVE')                              AS early_cnt,
       SUM(a.status = 'OUTING')                                   AS outing_cnt,
       SUM(a.status = 'ABSENT')                                   AS absent_cnt,
       SUM(a.status = 'EXCUSED')                                  AS excused_cnt
FROM member_tbl m
JOIN attendance_tbl a ON a.member_id = m.member_id
GROUP BY m.member_id, m.name, DATE_FORMAT(a.attend_date, '%Y-%m');
```

**결석 마감 대상 찾기** (LEFT JOIN + IS NULL — 7절의 핵심 쿼리):

```sql
SELECT m.member_id, m.name
FROM member_tbl m
LEFT JOIN attendance_tbl a
       ON a.member_id = m.member_id AND a.attend_date = ?
WHERE a.attendance_id IS NULL;
```

---

## 8. 출결 가져오기 — "파일 업로드"의 콘솔 버전

콘솔 앱에서 파일 업로드는 **경로를 입력받아 읽는 것**이다.

**왜 복사·붙여넣기인가** — 고용24에는 출결을 CSV·엑셀로 내려받는 기능이 없다(찾지 못했고, 없을 가능성이 높다). 훈련기관 창구에 요청하는 것은 선택지가 아니다. 그래서 **훈련생 각자가 자기 화면의 표를 복사해 온다.**

1. 고용24 로그인 → **마이페이지 → 훈련현황** → 우리 과정의 출결 화면을 연다.
2. 요약부터 세부내역 표 끝까지 **마우스로 드래그해 복사**한다. 요약이 함께 복사돼도 된다.
3. IntelliJ나 메모장에서 새 파일을 만들어 붙여 넣고 `data/` 아래에 **UTF-8 `.txt`** 로 저장한다 (예: `data/my-attendance.txt`).
4. 프로그램에서 메뉴 `3`을 고르고 그 경로를 입력한다.

```text
===== 출결 가져오기 =====
파일 경로: data/my-attendance.txt
세부내역 표 찾음: 15행 (오늘 1행 제외)   파싱 실패: 0
가져올까요? (y/n): y
→ 14행 저장, import_log #3 기록. (트랜잭션 커밋)
```

**복사한 텍스트의 모양** — 표의 칸은 **탭**으로 나뉘어 붙는다. 견본: [`data/sample.txt`](../data/sample.txt) (실제로 복사했을 때와 같은 모양, 이름과 값은 지어낸 것)

```text
[솔트룩스]LLM 지능형 에이전트 서비스개발 부트캠프
당일 출석결과는 익일 반영됩니다.
…  (요약 표, 설명 문구)
일자,입실시간,퇴실시간,수업시간,실수업시간,출결상태을(를) 제공하는 표
일자	입실시간	퇴실시간	수업시간	실수업시간	출결상태
2026-09-29	09:12	18:40	08:00	480 분	출석
2026-10-06	00:00	00:00	08:00	0 분	결석
2026-10-27	13:02	18:35	08:00	300 분	지각
```

**읽는 규칙**

- 위에서부터 읽다가 **탭으로 나눈 6칸이 정확히 `일자 입실시간 퇴실시간 수업시간 실수업시간 출결상태`인 줄**을 헤더로 본다. 그 위의 요약·설명은 버린다. 바로 위의 `…을(를) 제공하는 표` 줄은 쉼표로 이어진 설명이라 헤더가 아니다.
- 헤더 아래에서 `yyyy-MM-dd`로 시작하는 줄만 데이터로 읽는다. 빈 줄은 건너뛰고, 날짜로 시작하지 않는 줄이 나오면 표가 끝난 것으로 본다.
- 헤더를 찾지 못하면 "세부내역 표를 찾을 수 없습니다"를 보여 주고 중단한다. 세부내역까지 복사하지 않은 경우다.

| 열 | 값 모양 | 읽는 방법 |
| --- | --- | --- |
| `일자` | `2026-10-01` | `LocalDate.parse` |
| `입실시간`, `퇴실시간` | `09:12`, 기록이 없으면 `00:00` | `00:00`은 **기록 없음**(`null`)으로 읽는다 |
| `수업시간` | `08:00` | 그날 정해진 수업 시간 |
| `실수업시간` | `480 분` | 숫자만 떼어 분으로 읽는다 |
| `출결상태` | `출석` `지각` `조퇴` `외출` `결석` | `AttendanceStatus`로 바꾼다. 모르는 값이면 그 줄은 파싱 실패 |

- 표에 **훈련생 이름·전화번호 열이 없다.** 고용24는 본인 출결만 보여 주므로, 가져온 모든 줄은 **로그인한 훈련생**의 것으로 저장한다. 요약의 `훈련생명`은 저장에 쓰지 않는다.
- **오늘 이후 줄은 가져오지 않는다.** "당일 출석결과는 익일 반영"이라 오늘은 입실 시각이 있어도 퇴실 `00:00`, 상태 `결석`으로 보인다. 가져오더라도 계산에서는 빠진다 ([3절 계산 기준일](#3-출결-규칙-코드의-심장)).
- `조퇴`·`외출` 문구는 요약 표의 항목 이름으로 추정했다. 실제 줄에서 처음 보면 견본과 이 표를 고친다.

**견본의 기대값** — 견본은 우리 과정의 **훈련기간 전체(2026-09-29 ~ 2027-03-24)를 다 마친 상태**를 가정한다. 평일에서 공휴일(10/5 개천절 대체, 10/9 한글날, 12/25, 1/1, 2/8~2/9 설 연휴·대체, 3/1)을 빼면 고용24의 **총 훈련일수 120일**과 같다. 규칙 테스트의 기대값으로 쓴다.

| 출석 | 결석 | 지각 | 조퇴 | 외출 | 환산 결석 | 출석일 | 출석률 (출석일 / 120) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 109 | 4 (50% 미만 1 포함) | 3 | 2 | 2 | 4 + ⌊7 / 3⌋ = **6** | 120 − 6 = **114** | **95.0%** |

### 흐름과 책임

```text
Application  ──경로──▶  AttendanceController.importFile(path)
                           │ 1. Work24Importer.read(path)  → List<Attendance>   (파일 읽기 + 헤더 찾기 + 스트림 파싱, 실패 줄은 ImportException)
                           │ 2. 미리보기 출력, y/n 확인
                           │ 3. AttendanceRepository.saveAll(list, fileName, loginMemberId)
                           ▼        con.setAutoCommit(false)
                                    REPLACE INTO attendance_tbl ... × N
                                    INSERT INTO import_log_tbl ...
                                    con.commit()        ← 하나라도 SQLException이면 con.rollback()
```

- `data/` 폴더는 **gitignore** (개인 출결은 개인정보). `data/sample.txt`만 커밋.
- `출결상태`는 고용24가 판정한 값이므로 그대로 저장한다. `AttendanceRule`의 판정은 수동 입력(L-05)에만 쓴다.
- 한 줄이라도 파싱이나 저장에 실패하면 가져오기 전체를 중단한다 — "일부만 들어간 상태"를 만들지 않는 것이 트랜잭션의 요점.

---

## 9. 프로젝트 구조

수업에서 정한 **기준 구조**를 그대로 따르고, 이 프로젝트에 필요한 것만 **추가**한다.
패키지 이름은 기준 구조의 `com.프로젝트명.app`에 프로젝트 이름을 넣어 **`com.latecatcher.app`** 으로 한다.

### 기준 구조 (수업 공통)

```text
src/main
├─ java
│  └─ com.프로젝트명.app
│     ├─ run
│     │  └─ Application.java
│     ├─ model
│     │  ├─ Member.java
│     │  └─ ...
│     ├─ repository
│     │  ├─ MemberRepository.java
│     │  └─ ...
│     ├─ controller
│     │  ├─ MemberController.java
│     │  └─ ...
│     ├─ view
│     │  ├─ MemberView.java
│     │  └─ ...
│     ├─ mapper
│     │  ├─ member-query.xml
│     │  └─ ...
│     └─ common
│        └─ JDBCTemplate.java
└─ resources
   └─ db.properties
```

### 이 프로젝트의 구조 (`+` = 기준 구조에 추가한 것)

```text
java-latecatcher/
├─ build.gradle                         # mainClass → com.latecatcher.app.run.Application
├─ + data/
│  ├─ sample.txt                        # 커밋 (고용24 표를 복사한 모양의 견본)
│  └─ *                                 # gitignore (개인 출결)
├─ docs/  PLAN.md, CONVENTION.md
├─ .github/  ISSUE_TEMPLATE/, PULL_REQUEST_TEMPLATE.md
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ com.latecatcher.app
   │  │     ├─ run
   │  │     │  └─ Application.java
   │  │     ├─ model
   │  │     │  ├─ Member.java
   │  │     │  ├─ + Attendance.java
   │  │     │  ├─ + AttendanceStatus.java     # enum
   │  │     │  ├─ + Course.java
   │  │     │  └─ + MonthlySummary.java
   │  │     ├─ repository
   │  │     │  ├─ MemberRepository.java
   │  │     │  ├─ + AttendanceRepository.java
   │  │     │  ├─ + CourseRepository.java
   │  │     │  └─ + ClassDayRepository.java
   │  │     ├─ controller
   │  │     │  ├─ MemberController.java
   │  │     │  └─ + AttendanceController.java
   │  │     ├─ view
   │  │     │  ├─ MemberView.java
   │  │     │  └─ + AttendanceView.java
   │  │     ├─ mapper
   │  │     │  ├─ member-query.xml
   │  │     │  ├─ + attendance-query.xml
   │  │     │  ├─ + course-query.xml
   │  │     │  └─ + classday-query.xml
   │  │     ├─ common
   │  │     │  └─ JDBCTemplate.java
   │  │     ├─ + rule                         # 추가 패키지
   │  │     │  └─ AttendanceRule.java
   │  │     └─ + importer                     # 추가 패키지
   │  │        ├─ AttendanceImporter.java
   │  │        ├─ Work24Importer.java
   │  │        └─ ImportException.java
   │  └─ resources
   │     ├─ db.properties                   # gitignore (이미 설정됨)
   │     ├─ + db.properties.example
   │     └─ + db
   │        ├─ schema.sql
   │        └─ insert.sql
   └─ + test
      └─ java
         └─ com.latecatcher.app
            └─ rule
               └─ AttendanceRuleTest.java
```

### 추가한 것과 이유

| 추가 | 위치 | 이유 |
| --- | --- | --- |
| `rule` 패키지 | `com.latecatcher.app.rule` | 출결 규칙(상태 판정, 출석률 계산)을 DB와 콘솔 없이 **순수 Java**로 두어 JUnit으로 테스트하기 위해. controller에 두면 테스트할 때 DB가 필요해진다 |
| `importer` 패키지 | `com.latecatcher.app.importer` | 붙여 넣은 표 읽기·파싱은 DB 접근(repository)도 흐름 조정(controller)도 아니라서 따로 둔다 |
| 모델·repository·controller·view·mapper 파일 | 기준 패키지 안 | 출결·과정·수업일 기능. 기준 패키지에 파일만 추가한다 |
| `db.properties.example` | `resources` | `db.properties`는 gitignore라서, 팀원이 복사해 쓸 견본을 커밋한다 |
| `db/schema.sql`, `db/insert.sql` | `resources/db` | 7절 스키마와 샘플 데이터 |
| `data/` | 프로젝트 루트 | 출결 가져오기 입력 파일(고용24 표를 붙여 넣은 텍스트). `sample.txt`만 커밋 |
| `src/test` | 프로젝트 루트 | `AttendanceRuleTest` (13절 성공 기준) |

### 규칙

- **기준 구조의 패키지 7개(`run`, `model`, `repository`, `controller`, `view`, `mapper`, `common`)는 이름과 위치를 바꾸지 않는다.**
- 새 패키지가 필요하면 이 절의 "추가한 것과 이유" 표에 이유와 함께 먼저 적고, PR에서 리뷰받는다.
- `service` 계층은 만들지 않는다 (6절). 규칙은 `rule`, 흐름은 `controller`에 둔다.
- mapper XML은 기준 구조대로 `java` 아래 패키지 폴더에 둔다. repository가 **파일 경로 문자열**로 읽기 때문에, 패키지 이름을 바꾸면 각 repository의 `QUERY_FILE` 경로도 함께 바꾼다.

  ```java
  // 변경 전 (스켈레톤)
  private static final String QUERY_FILE = "src/main/java/skeleton/mapper/member-query.xml";
  // 변경 후
  private static final String QUERY_FILE = "src/main/java/com/latecatcher/app/mapper/member-query.xml";
  ```

---

## 10. 콘솔 메뉴

스켈레톤의 "시작 메뉴 → 로그인 → 기능 메뉴" 흐름을 유지한다.

```text
===== 지각 잡는 자바 =====
1. 회원가입
2. 로그인
9. 프로그램 종료

===== 이영희 — 출결 메뉴 =====
1. 내 출결 리스크 보기 (이번 달)
2. 출결 입력 (날짜/입실/퇴실)
3. 출결 가져오기 (고용24 표 붙여 넣은 파일)
4. 반 전체 현황 (월별 순위, 80% 미달자)
5. 결석 일괄 마감 (지난 수업일)
6. 과정 설정 / 수업일 캘린더
7. 회원 탈퇴
8. 로그아웃
9. 프로그램 종료
```

---

## 11. 팀 구성과 역할 분담 (3명)

규칙은 [CONVENTION.md](CONVENTION.md). **층이 아니라 기능으로 세로 분할** — 한 사람이 한 기능을 model → repository → controller → view까지 만든다.

| 담당 | 기능 | 만드는 것 | 집중 실습 항목 |
| --- | --- | --- | --- |
| **A — 훈련생 + 출결 가져오기** | 회원 CRUD 마무리, **출결 가져오기** | `Work24Importer`, `ImportException`, `AttendanceRepository.saveAll`, `import_log_tbl` | 파일 읽기, 스트림 파싱, **JDBC 트랜잭션**, REPLACE, (선택) executeBatch |
| **B — 출결 + 규칙** | 출결 수동 입력, 상태 판정, **결석 일괄 마감**, 중복 입력 처리 | `Attendance`, `AttendanceStatus`, `AttendanceRule` + **JUnit 테스트**, `AttendanceRepository.save/findMissingOnDate` | enum, 순수 로직 + 단위 테스트, LEFT JOIN + IS NULL, UNIQUE 위반 예외 구분, 트랜잭션(마감) |
| **C — 캘린더 + 통계 + 통합** | 과정 설정, 수업일 생성, **리스크 화면**, 반 전체 현황, `Application` 메뉴 통합, README | `Course`, `ClassDayRepository`, `MonthlySummary`, `v_monthly_attendance` 뷰, `AttendanceView`, `run.Application` | java.time 평일 생성, VIEW, GROUP BY/HAVING, SUM(CASE), INDEX + EXPLAIN |

> A·B·C가 `AttendanceRepository`와 `attendance-query.xml`을 함께 건드린다. 메서드가 안 겹치면 git이 합치고, 겹치면 **첫 충돌 해결 연습**. B가 먼저 `AttendanceRepository` 파일과 `save`를 만들고 머지하면 충돌이 줄어든다.

### 0단계 — 함께 먼저 (각자 이슈 1개, 전체 흐름을 한 번씩 연습)

| 이슈 | 담당 | 내용 | 라벨 |
| --- | --- | --- | --- |
| #1 | A | 패키지 `skeleton` → `com.latecatcher.app` 변경 (repository의 `QUERY_FILE` 경로 포함, 9절), `build.gradle` mainClass 수정, **`build.gradle`·Gradle 래퍼를 수업 버전(Java 21, Gradle 9.6.0, Lombok 설정)으로 맞춤 (12절)**, `MemberRepository.save` 문법 오류 수정, `db.properties.example`, `data/` gitignore | `🌏 Environment` `config` |
| #2 | B | `resources/db/schema.sql` (7절) + `insert.sql` (팀 3명, 10월 수업일, 샘플 출결), `model/Attendance`, `AttendanceStatus`, `Course` | `🌏 Environment` `sql` `model` |
| #3 | C | 이슈 템플릿 정리 (파일명 오타 `TAMPLATE`, 라벨 `enhancement` → `✨ Feature`, bug/refactor/environment/suggestion 추가 — PR 템플릿은 정본이므로 그대로), 라벨 생성, main 브랜치 보호(approve 2), README 초안, `Application` 메뉴 뼈대("준비 중") | `🌏 Environment` `run` `docs` |

model을 0단계에 두는 이유: 셋 모두 `Attendance`를 쓰므로 **필드 이름을 먼저 합의**해야 한다. 7절 스키마가 기준.

### 흐름

```text
0단계 (3 PR) ─┬─ A: 출결 가져오기 ──────┐
              ├─ B: 출결 입력·판정·마감 ─┼─ C: 메뉴 통합 ─ [v0.1] ─ v0.2 (공휴일 API, 텔레그램: 이슈로 나눠 각자) ─ [v0.2]
              └─ C: 캘린더·통계 ─────────┘
```

- 기능 1개 = 이슈 1개 = 브랜치 1개 = PR 1개. 크면 쪼갠다 (A: "파일 읽기·미리보기" / "트랜잭션 저장" 2개로).
- 매일 10분 모여 열린 PR을 함께 보고 approve. 3명이라 approve 2 = **나머지 둘이 모두 봐야 머지**.

---

## 12. 기술 선택

**원칙: 기술과 버전은 수업에서 쓴 것을 그대로 따른다.** 기준은 `03_jdbc`의 `05-JDBC`, `06-JDBC-Exercise` 프로젝트 설정이다. 스켈레톤이 수업과 다르면 수업에 맞춰 고친다 (0단계 #1).

| 항목 | 수업 | 스켈레톤 (현재) | 프로젝트 |
| --- | --- | --- | --- |
| JDK | **Temurin 21** (IntelliJ SDK `temurin-21`, language level 21) | `build.gradle`에 Java 17 | **21** |
| 빌드 | Gradle, 래퍼 **9.6.0** | 래퍼 9.3.0 | **9.6.0** |
| DB 드라이버 | `com.mysql:mysql-connector-j:9.7.0` | 같음 | 그대로 |
| Lombok | `compileOnly` + `annotationProcessor` `1.18.46` (테스트는 `testCompileOnly` + `testAnnotationProcessor` `1.18.48`) | `runtimeOnly`가 하나 더 있고 테스트용이 없음 | 수업 설정 그대로 |
| 테스트 | JUnit `junit-bom:6.0.0` + `junit-jupiter`, `useJUnitPlatform()` | 같음 | 그대로 |
| DB | MySQL 8.0, `com.mysql.cj.jdbc.Driver`, `db.properties` | 같음 | 그대로 |
| 개발 환경 | macOS + IntelliJ IDEA | — | 그대로 |

- 수업에서 배우지 않은 라이브러리는 꼭 필요할 때만 넣고, 넣을 때는 이유를 PR에 적는다.
- `java.net.http.HttpClient`(v0.2 API 호출)는 JDK 11부터 들어 있어서 따로 추가하지 않는다.
- v0.2에서만 추가: **Gson** (JSON), `api.properties` (gitignore)
- SQL은 `mapper/*.xml` / 자원은 try-with-resources / `SQLException` → `IllegalStateException` — 스켈레톤 규칙 유지
- 날짜: `getObject(col, LocalDate.class)` 방식으로 통일 (스켈레톤의 `getTimestamp().toLocalDateTime()`은 `DATETIME` 컬럼이 없어지므로 쓰지 않음)

---

## 13. 성공 기준

- [ ] 팀원 3명이 로그인해서 **자기 10월 출석률과 경고**를 본다 — 수치가 수동 계산과 일치
- [ ] 출결 가져오기 파일의 중간 줄을 고의로 깨뜨리면 **한 줄도 저장되지 않는다** (롤백 확인 기록이 PR에 있음)
- [ ] 같은 날 중복 입력 시 "이미 기록이 있습니다" 안내 (예외 스택트레이스 아님)
- [ ] 반 전체 현황에서 80% 미달자가 HAVING으로 걸러져 나온다
- [ ] `AttendanceRuleTest` 가 통과한다 (경계값: 지각 2회/3회, 79.9%/80.0%, 공결 처리, **퇴실 기록 없는 오늘 줄은 계산에서 빠짐**)
- [ ] 팀원 각자 PR 2개 이상, 모든 PR approve 2개, 커밋 메시지 규칙 준수, 태그 `v0.1`
- [ ] README만 보고 다른 반 친구가 clone → `schema.sql` → `db.properties` → 실행 → 자기 고용24 표를 복사해 가져오기까지 할 수 있다

---

## 14. 열린 질문 (정해지면 이 문서를 고친다)

1. **출결 규칙 확정** — 지각 환산(3회=1일?), 단위기간(월? 30일?), 장려금·수료 기준 %, 공결 처리. → 훈련기관 안내문 확인 후 3절 표와 `course_tbl` 기본값 수정
2. ~~**고용24 출결현황 화면의 실제 열** — 첫 데이터를 받아 보고 8절 형식 확정~~ → **결정: 세부내역 표 6열 그대로, 복사한 텍스트를 탭으로 읽는다** ([8절](#8-출결-가져오기--파일-업로드의-콘솔-버전), `data/sample.txt`)
3. ~~Java 17 유지 vs 21로 통일~~ → **결정: 수업과 같은 Java 21** ([12절](#12-기술-선택))
4. `Member.age` 유지 여부 (스켈레톤 그대로 두는 쪽을 제안)
5. A/B/C 담당자
6. 머지 방식 merge commit vs squash (CONVENTION은 merge commit 제안)
7. v0.2에서 공휴일 API와 텔레그램 중 무엇을 먼저 할지

---

## 15. 업무 분담 — 기능 + 문서 (v0.1)

[4절](#4-기능)의 기능과 노션 문서 작업을 함께 묶어 세 사람의 업무량이 비슷하도록 나눈다.
시간은 현재 코드 상태와 이 문서를 보고 잡은 **추정치**다. 이슈를 만들 때 각자 예상 시간을 다시 적고, 합계가 크게 어긋나면 이 표를 고친다.

### A — 훈련생 + 출결 가져오기 + 캘린더 (약 16.5h)

| 작업 | 예상 시간 |
| --- | --- |
| 0단계 #1: 패키지 이름 `com.latecatcher.app`으로 변경 (`QUERY_FILE` 경로 포함), mainClass 수정, 빌드 설정 수업 버전으로 맞춤 (Java 21, Gradle 9.6.0), `MemberRepository` 문법 오류 수정, `db.properties.example` | 1.5h |
| L-02 로그인 버그 수정, L-10 로그아웃, L-11 회원 탈퇴 마무리 | 1.5h |
| L-06 출결 가져오기 | 6h |
| L-04 수업일 캘린더 | 3h |
| 노션 **요구사항 명세** 페이지 (기능 명세) | 2h |
| 노션 모델링 페이지 — **properties 파일** | 1h |
| 노션 **팀 R&R**, ⭐**Ground Rule** 페이지 | 1.5h |

### B — 출결 + 규칙 (약 16h)

| 작업 | 예상 시간 |
| --- | --- |
| 0단계 #2: `schema.sql`, `insert.sql`, `Attendance`, `AttendanceStatus`, `Course` | 3h |
| L-05 출결 입력 + 상태 판정 (`AttendanceRule`) + JUnit | 4h |
| 출석률·환산 결석 계산 (`AttendanceRule`) + JUnit | 2h |
| L-07 결석 일괄 마감 | 3h |
| 노션 모델링 페이지 — **테이블 정의서** | 2h |
| **ERD** | 1.5h |
| 노션 **데일리 스크럼** 페이지 (템플릿) | 0.5h |

### C — 설정 + 화면 + 통합 (약 17h)

| 작업 | 예상 시간 |
| --- | --- |
| 0단계 #3: 이슈 템플릿, 라벨, 브랜치 보호, `Application` 메뉴 뼈대 | 2.5h |
| L-03 과정 설정 | 3h |
| L-08 내 출결 리스크 | 4h |
| L-09 반 전체 현황 | 3h |
| L-12 프로그램 종료 + 메뉴 통합 | 2h |
| README 마무리 | 1h |
| 노션 **프로젝트 소개** 페이지 (아이디어 → 기능) | 1.5h |

### 나눈 기준

- **기능은 세로로 나눈다.** 한 사람이 한 기능을 model → repository → controller → view까지 만든다 ([11절](#11-팀-구성과-역할-분담-3명) 원칙).
- **문서는 자기가 만드는 코드와 맞물리게 맡는다.** 스키마를 만드는 B가 테이블 정의서와 ERD를 함께 맡아 둘이 스키마와 어긋나지 않게 한다. 설정 파일을 다루는 A가 properties 설명을, 전체 흐름을 묶는 C가 프로젝트 소개를 쓴다.
- **11절과 다른 점이 두 가지 있다.**
  - L-04 수업일 캘린더를 C에서 A로 옮겼다. A의 회원 기능은 대부분 main에 머지돼 여유가 있고, C는 기능 수가 가장 많다.
  - 출석률·환산 결석 계산을 B가 맡는다고 명시했다. 판정 규칙과 같은 `AttendanceRule`에 있으므로 B가 테스트까지 만들고, C의 L-08·L-09는 그 결과를 화면에 보여 주기만 한다.

### 진행 순서

C의 L-08·L-09와 메뉴 통합은 A·B의 결과가 있어야 확인할 수 있다. 대기 시간을 줄이려고 다음 순서로 간다.

1. B가 `AttendanceRule`(판정 + 출석률 계산)을 먼저 머지한다.
2. 그동안 C는 L-03 과정 설정과 노션 프로젝트 소개 페이지를 한다.
3. A·B 기능이 머지되면 C가 L-08·L-09와 메뉴 통합을 한다.

### 담당자 제안 (14절 5번이 정해지면 고친다)

스켈레톤에서 다뤄 본 코드와 가장 비슷한 일을 맡는 방식이다.

| 역할 | 제안 | 이유 |
| --- | --- | --- |
| A | RY | 스켈레톤 회원 기능(F-01, F-02, F-09, F-10) 담당 |
| B | YY | 스켈레톤 구독 추가·연장·삭제(F-04, F-06, F-07) 담당 — 쓰기와 갱신 |
| C | YI | 스켈레톤 조회 기능(F-03, F-05, F-08) 담당 — 조회와 출력 |

> 데일리 스크럼 페이지는 템플릿 작성만 B의 시간에 넣었다. 매일 진행은 돌아가며 맡는다.
