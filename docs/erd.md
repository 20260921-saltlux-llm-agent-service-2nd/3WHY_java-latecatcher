# ERD — 지각 잡는 자바

> **상태: 합의본 (10/22 ERD 회의).** 스키마 원본(DDL)은 [PLAN.md 7절](PLAN.md#7-데이터-설계)이고, 이 문서는 그 스키마를 그림과 설명으로 풀어 쓴 것이다.
> **회의 결정:** ① 로그인은 **휴대폰 번호**로 한다. 아이디·비밀번호는 두지 않는다 (스켈레톤 방식). ② **과정과 회원은 잇지 않는다.** `course_tbl`은 다른 테이블과 FK로 잇지 않는 1행 설정표다. ③ **회원은 스켈레톤 그대로**(이름·나이·휴대폰 번호)다. 나이(`age`)를 지우지 않는다.
> 회의 전 확장 초안(비밀번호 로그인, 회원–과정 FK)은 [erd-extended.md](erd-extended.md)와 그림 [erd-extended.excalidraw](erd-extended.excalidraw)에 보관한다. 확장할 때 출발점으로 쓴다.
> Mermaid `erDiagram`이 처음이면 [부록: Mermaid erDiagram 문법](#부록-mermaid-erdiagram-문법)을 먼저 읽는다.

---

## 1. 전체 ERD

`attendance_tbl`을 가운데 두고 그렸다. 스키마 모양이 **스타 스키마**(가운데 팩트 테이블 + 둘레의 차원 테이블)와 비슷하기 때문이다.

| 스타 스키마 | 이 ERD | 이유 |
| --- | --- | --- |
| **팩트** (일어난 일, 1행 = 사건 1건) | `attendance_tbl` | 1행 = "누가 어느 날 몇 시에 와서 어떤 상태였나". 행이 날마다 늘어나고 집계(6절 뷰)의 대상이다 |
| **차원** (누가) | `member_tbl` | 출결을 "누구의 것"으로 묶는다 |
| **차원** (언제) | `class_day_tbl` | 출결을 "어느 수업일의 것"으로 묶는다. 날짜 차원과 같은 역할 |
| 팩트 집계 | `v_monthly_attendance` (6절) | 팩트를 회원 × 월로 묶어 상태별 개수를 구한다 |
| 스타 바깥 | `import_log_tbl`(가져오기 기록), `course_tbl`(설정 1행) | 출결과 FK로 잇지 않는다. `course_tbl`은 어떤 테이블과도 잇지 않는다 |

닮은 것은 **모양**뿐이다. 데이터 웨어하우스의 스타 스키마는 조회를 빠르게 하려고 차원을 일부러 비정규화하기도 한다. 이 스키마는 입력·수정을 위한 설계라 3NF까지 정규화했다 (4절).

Mermaid는 표를 자동으로 배치한다. 아래 그림은 가져오기 기록 → 차원(회원·수업일) → 팩트 → 뷰 순서로 놓이고, 관계가 없는 `course_tbl`의 자리는 정할 수 없다 ([부록: 배치](#배치--표의-위치는-직접-정할-수-없다)).

```mermaid
---
config:
  er:
    layoutDirection: LR
---
erDiagram
    import_log_tbl }o..o| member_tbl     : "가져오기 실행 (비식별, v0.2)"
    member_tbl     ||..o{ attendance_tbl : "출결을 남긴다 (비식별)"
    class_day_tbl  ||..o{ attendance_tbl : "그날의 출결 (비식별)"
    attendance_tbl }|..|| v_monthly_attendance : "뷰가 읽음: 한 달 치를 1행으로 묶는다"
    member_tbl     ||..o{ v_monthly_attendance : "뷰가 읽음: 이름"

    member_tbl {
        INT         member_id PK "AUTO_INCREMENT"
        VARCHAR(30) name         "NOT NULL, 성과 이름을 띄어쓰기 없이 (예: 홍길동)"
        INT         age          "NOT NULL, 1 이상 (Java 확인, 스켈레톤)"
        VARCHAR(20) phone     UK "NOT NULL, 로그인 키, CHECK 010-1234-5678 형식"
        ENUM        role         "ADMIN | TRAINEE, 기본 TRAINEE (v0.2 L-16)"
    }

    course_tbl {
        INT          course_id            PK "AUTO_INCREMENT, 1행만 사용"
        VARCHAR(100) name                    "NOT NULL"
        DATE         start_date              "NOT NULL"
        DATE         end_date                "NOT NULL, CHECK start_date 이후"
        TIME         start_time              "NOT NULL, 기본 09:20 (slx-ai-campus)"
        TIME         end_time                "NOT NULL, 기본 18:20 (slx-ai-campus)"
        INT          partial_per_absence     "NOT NULL, 기본 3 (지각·조퇴·외출 n회 = 결석 1일)"
        INT          min_attend_minutes      "NOT NULL, 기본 240 (최소 수업 시간 분. 미만이면 결석)"
        INT          allowance_threshold     "NOT NULL, 기본 80 (장려금 기준 %)"
        INT          completion_threshold    "NOT NULL, 기본 80 (수료 기준 %)"
    }

    class_day_tbl {
        DATE         class_date   PK "날짜 자체가 키"
        BOOLEAN      is_class_day    "NOT NULL, 기본 TRUE (FALSE = 공휴일·휴강)"
        VARCHAR(255) note            "NULL 허용, 예: 개천절, 휴강"
    }

    attendance_tbl {
        INT          attendance_id PK     "AUTO_INCREMENT"
        INT          member_id     FK, UK "NOT NULL, (member_id, attend_date) UNIQUE"
        DATE         attend_date   FK, UK "NOT NULL, INDEX idx_attendance_tbl_date"
        TIME         check_in             "NULL 허용"
        TIME         check_out            "NULL 허용, CHECK check_in 이후"
        ENUM         status               "NOT NULL, PRESENT | LATE | EARLY_LEAVE | LATE_EARLY_LEAVE | OUTING | ABSENT | EXCUSED"
        VARCHAR(255) memo                 "NULL 허용"
        ENUM         source               "MANUAL | WORK24, 기본 MANUAL (v0.2 L-06)"
    }

    import_log_tbl {
        INT          import_id   PK "AUTO_INCREMENT"
        VARCHAR(255) file_name      "NOT NULL"
        INT          row_count      "NOT NULL"
        DATETIME     imported_at    "NOT NULL, 기본 CURRENT_TIMESTAMP"
        INT          imported_by FK "NULL 허용, ON DELETE CASCADE"
    }

    v_monthly_attendance {
        INT         member_id   "VIEW, member_id + ym 이 한 행"
        VARCHAR(30) name        "member_tbl.name"
        CHAR(7)     ym          "YYYY-MM"
        INT         record_cnt  "기록 일수 (행 수)"
        INT         present_cnt "PRESENT 횟수"
        INT         late_cnt    "LATE + LATE_EARLY_LEAVE 횟수"
        INT         early_cnt   "EARLY_LEAVE + LATE_EARLY_LEAVE 횟수"
        INT         outing_cnt  "OUTING 횟수"
        INT         absent_cnt  "ABSENT 횟수"
        INT         excused_cnt "EXCUSED 횟수"
    }
```

- **`v_monthly_attendance`는 테이블이 아니라 뷰다.** 데이터를 저장하지 않고, 조회할 때 `member_tbl`과 `attendance_tbl`을 읽어 상태별 개수를 구한다. 그래서 PK·FK가 없다. 뷰로 가는 두 선은 FK 관계가 아니라 **"뷰가 읽는다"** 는 뜻이다. Mermaid에는 뷰 전용 선이 없어 점선으로 그리고, 관계 이름 앞에 `뷰가 읽음`을 붙여 구분한다. 자세한 것은 [6절](#6-뷰--v_monthly_attendance).
- **`course_tbl`은 아무 테이블과도 잇지 않는다 (회의 결정).** 과정 설정 1행만 두는 설정표다. 출결 규칙(`AttendanceRule`)이 이 행의 시각·기준 %를 읽어 판정한다. 과정이 하나뿐이라 "어느 과정의 출결인가"의 답은 늘 이 한 행이다 ([4절 3NF](#3nf--이행-함수-종속을-없앤다)).
- **v0.2용인 `role`, `source`, `import_log_tbl`도 미리 둔다.** v0.1 코드는 이 셋을 쓰지 않는다. 확장을 생각한 결정이다 ([PLAN.md 7절](PLAN.md#7-데이터-설계), 14절 10번).
- **자유 입력 메모 열(`note`, `memo`)은 `VARCHAR(255)`로 통일한다.**
- 테이블 사이의 관계선은 모두 **점선 = 비식별 관계**다. 이유는 3절에 있다.

## 2. 테이블 한눈에

| 테이블 | 한 행의 뜻 | 쓰는 기능 | 버전 |
| --- | --- | --- | --- |
| `member_tbl` | 회원(훈련생) 1명 | L-01·L-02 회원가입·로그인, L-11 탈퇴 | v0.1 (`role`은 v0.2) |
| `course_tbl` | 과정 설정 (1행만) | L-03 과정 설정, 출결 규칙 | v0.1 |
| `class_day_tbl` | 달력의 하루 (수업일인지 아닌지) | L-04 수업일 캘린더, L-05 출결 입력 확인 | v0.1 |
| `attendance_tbl` | 훈련생 1명의 하루 출결 | L-05 출결 입력, L-08 리스크, L-09 반 전체 현황 | v0.1 (`source`는 v0.2) |
| `import_log_tbl` | 출결 가져오기 1회 | L-06 출결 가져오기 | v0.2 |
| `v_monthly_attendance` (뷰) | 훈련생 1명의 한 달 상태별 횟수 | L-09 반 전체 현황 | v0.1 |

### `member_tbl`의 로그인 — 휴대폰 번호

회원은 **휴대폰 번호로 로그인**한다. 아이디와 비밀번호 열은 없다. 스켈레톤의 회원가입·로그인 방식(F-01, F-02)과 회원 열(이름·나이·휴대폰 번호)을 그대로 쓴다.

| 열 | 역할 | 제약 |
| --- | --- | --- |
| `member_id` | 회원을 가리키는 **대리 키**(PK). 출결·가져오기 기록이 FK로 가리킨다 | `AUTO_INCREMENT` |
| `phone` | 회원을 **찾는** 키. 로그인할 때 이 값으로 조회한다 | `NOT NULL` + `UNIQUE` + `CHECK` 형식 `010-1234-5678` |

#### 가입과 로그인의 흐름

```text
가입 (L-01)   이름, 나이, 휴대폰 번호를 입력받는다
              Java가 빈 값·나이(1 이상)·번호 형식(010-1234-5678)을 확인한다 → 틀리면 다시 입력
              findByPhone으로 이미 있는 번호인지 확인한다 → 있으면 "이미 등록된 휴대폰 번호입니다"
              member_tbl에 INSERT (형식은 CHECK, 중복은 UNIQUE가 한 번 더 지킨다)

로그인 (L-02) 휴대폰 번호를 입력받는다
              findByPhone으로 조회한다 → 있으면 로그인 상태로 보관, 없으면 안내 후 시작 메뉴로
```

#### 휴대폰 번호를 PK로 쓰지 않는 이유

`phone`은 `NOT NULL` + `UNIQUE`라서 PK가 될 수 있는 **후보 키**다. 그래도 PK는 `member_id`로 둔다.

- 휴대폰 번호는 **바뀔 수 있다.** PK가 바뀌면 그 값을 FK로 가진 출결·가져오기 기록도 모두 바꿔야 한다.
- 대리 키 `member_id`는 바뀌지 않는다. 번호가 바뀌어도 `member_tbl`의 한 행만 고치면 된다.

> 알아 둘 점: 비밀번호가 없으므로 **번호를 아는 사람은 그 회원으로 로그인할 수 있다.** 비밀번호 로그인은 확장안([erd-extended.md](erd-extended.md)의 `password_hash`·`password_salt`)에 설계해 두었다.

## 3. 관계 — 식별 관계와 비식별 관계

| 구분 | 뜻 | 선 |
| --- | --- | --- |
| **식별 관계** | 부모의 PK가 자식 **PK의 일부**가 된다. 부모 없이는 자식 행을 구별할 수 없다 | 실선 (Mermaid `--`) |
| **비식별 관계** | 부모의 PK가 자식의 **일반 열(FK)** 로만 들어간다. 자식은 자기 PK로 구별된다 | 점선 (Mermaid `..`) |

| 부모 | 자식 | FK 열 | 카디널리티 | 관계 | 부모를 지우면 | 뜻 |
| --- | --- | --- | --- | --- | --- | --- |
| `member_tbl` | `attendance_tbl` | `member_id` | 1 : 0..N | 비식별 | **CASCADE** — 출결도 지운다 | 회원은 출결을 0건 이상 남긴다. 출결은 반드시 회원 1명의 것이다 |
| `class_day_tbl` | `attendance_tbl` | `attend_date` | 1 : 0..N | 비식별 | 막힌다 (RESTRICT, 오류 1451) | 출결 날짜는 캘린더에 있는 날이어야 한다. 하루에 훈련생 수만큼 출결이 생긴다 |
| `member_tbl` | `import_log_tbl` | `imported_by` | 0..1 : 0..N | 비식별 | **CASCADE** — 가져오기 기록도 지운다 | 가져오기 기록에 실행한 회원을 남긴다. 비어 있어도 된다 |

`course_tbl`은 이 표에 없다. 어떤 테이블과도 FK로 잇지 않는다.

**모두 비식별인 이유:** `attendance_tbl`은 `attendance_id`, `import_log_tbl`은 `import_id`라는 **대리 키**(의미 없는 일련번호)를 PK로 쓴다. 부모 키(`member_id`, `attend_date`)는 PK에 들어가지 않고 FK로만 있다.

**하루 한 기록:** 정규화하면 출결의 진짜 키는 `(member_id, attend_date)`다 (4절 2NF). 이것을 PK 대신 `UNIQUE`로 지킨다. 그래서 대리 키를 써도 같은 날 두 건은 들어갈 수 없다.

### 식별 관계로 바꾼다면 (대안)

`attendance_id`를 없애고 `(member_id, attend_date)`를 PK로 삼으면 두 관계가 **식별 관계(실선)** 가 된다.

```mermaid
erDiagram
    member_tbl    ||--o{ attendance_tbl : "출결을 남긴다 (식별)"
    class_day_tbl ||--o{ attendance_tbl : "그날의 출결 (식별)"

    attendance_tbl {
        INT          member_id   PK, FK "부모 키가 PK의 일부"
        DATE         attend_date PK, FK "부모 키가 PK의 일부"
        TIME         check_in
        TIME         check_out
        ENUM         status
        VARCHAR(255) memo
        ENUM         source
    }
```

| | 지금 (비식별 + 대리 키) | 대안 (식별 + 복합 PK) |
| --- | --- | --- |
| 하루 한 기록 | `UNIQUE (member_id, attend_date)` | PK가 그대로 보장 |
| 열 수 | `attendance_id` 1열 더 | 1열 적다 |
| 행 하나를 가리킬 때 | `attendance_id` 하나 | `member_id` + `attend_date` 둘 |
| 스켈레톤·JDBC | 스켈레톤과 같은 `AUTO_INCREMENT` + `getGeneratedKeys` | 생성 키가 없다. 모델·쿼리를 스켈레톤과 다르게 짠다 |
| 나중에 출결을 참조하는 테이블이 생기면 | FK 1열 | FK 2열 |

지금 스키마는 **비식별 + 대리 키**다. 스켈레톤 스타일과 같고, 하루 한 기록은 `UNIQUE`가 똑같이 지킨다. 정규화 결과(4절)는 식별 관계이고, 구현에서 대리 키로 바꾼 것이다.

## 4. 정규화 — 3NF까지

출결을 **엑셀 출결 대장 한 장**에 적는다고 생각하고, 그 표를 3NF까지 나누면 지금의 테이블이 나온다.

### 출발점 — 출결 대장 한 장 (비정규형)

| 휴대폰 | 이름 | 과정명 | 수업 시작 | 수업 종료 | 출결 |
| --- | --- | --- | --- | --- | --- |
| 010-1111-2222 | 홍길동 | 자바 과정 | 09:20 | 18:20 | 10/20 출석 09:15~18:21, 10/21 지각 09:32~18:20 |
| 010-3333-4444 | 김철수 | 자바 과정 | 09:20 | 18:20 | 10/20 결석, 10/21 출석 09:10~18:25 |

`출결` 칸 하나에 여러 날이 들어 있다 (**반복 그룹**). "10/21에 지각한 사람"을 찾으려면 문자열을 잘라 봐야 한다.

### 1NF — 모든 칸에 값 하나

하루를 한 줄로 펼친다. 이제 한 줄을 구별하는 키는 **(휴대폰, 날짜)** 다. 휴일 메모도 같은 표에 적는다고 하자.

| **휴대폰** | **날짜** | 이름 | 과정명 | 수업 시작 | 수업 종료 | 수업일? | 휴일 메모 | 입실 | 퇴실 | 상태 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 010-1111-2222 | 10/20 | 홍길동 | 자바 과정 | 09:20 | 18:20 | Y | | 09:15 | 18:21 | 출석 |
| 010-1111-2222 | 10/21 | 홍길동 | 자바 과정 | 09:20 | 18:20 | Y | | 09:32 | 18:20 | 지각 |
| 010-3333-4444 | 10/20 | 김철수 | 자바 과정 | 09:20 | 18:20 | Y | | | | 결석 |

남은 문제: 이름·과정이 **날마다 반복**된다. 이름을 고치면 모든 줄을 고쳐야 하고(수정 이상), 출결이 없으면 회원을 적을 곳이 없다(삽입 이상).

### 2NF — 부분 함수 종속을 없앤다

키 **(휴대폰, 날짜)의 일부에만** 결정되는 열을 떼어 낸다.

| 결정하는 것 | 결정되는 열 | 떼어 낸 테이블 |
| --- | --- | --- |
| 휴대폰 하나 | 이름, 나이, 과정명, 수업 시작, 수업 종료 | 회원 → `member_tbl` |
| 날짜 하나 | 수업일?, 휴일 메모 | 수업일 → `class_day_tbl` |
| (휴대폰, 날짜) 전체 | 입실, 퇴실, 상태, 메모 | 출결 → `attendance_tbl` |

이 단계의 출결 테이블은 PK가 `(휴대폰, 날짜)`이고, 둘 다 부모를 가리킨다. 곧 **식별 관계**다 (3절 대안). 구현에서는 번호가 바뀔 수 있으므로 회원에 대리 키 `member_id`를 두고, 출결은 `member_id`로 회원을 가리킨다 (2절).

### 3NF — 이행 함수 종속을 없앤다

키가 아닌 열이 **다른 키 아닌 열을 거쳐** 결정되면 떼어 낸다.

- **회원 표:** `휴대폰 → 과정명 → 수업 시작, 수업 종료, 기준 %`. 수업 시각은 회원이 아니라 과정이 정한다. 그래서 과정을 `course_tbl`로 뗀다.
  - **과정은 하나뿐이고, 회의에서 과정과 회원을 잇지 않기로 했다.** 그래서 `member_tbl`에 `course_id`를 두지 않고, `course_tbl`은 1행짜리 설정표로 남는다. 모든 회원과 출결의 과정은 이 한 행이다.
  - 과정이 여럿이 되면 회원(또는 출결)이 과정을 가리켜야 한다. 그 설계는 확장안([erd-extended.md](erd-extended.md))에 있다.
- **출결 표에 대리 키를 붙인 뒤:** 출결 표에 이름을 함께 적으면 `attendance_id → member_id → 이름`이 되어 3NF가 깨진다. 그래서 `attendance_tbl`에는 이름을 두지 않는다. 이름이 필요한 화면은 `JOIN`이나 뷰(6절)로 가져온다.

### 결과 점검

| 테이블 | 1NF | 2NF | 3NF | 근거 |
| --- | --- | --- | --- | --- |
| `member_tbl` | ✅ | ✅ | ✅ | 키가 한 열(`member_id`)이라 부분 종속이 없다. 모든 열이 회원 자신을 설명한다. `phone`은 또 하나의 후보 키(NOT NULL + UNIQUE) |
| `course_tbl` | ✅ | ✅ | ✅ | 모든 열이 과정 설정이다 |
| `class_day_tbl` | ✅ | ✅ | ✅ | 키가 날짜 한 열. 수업일 여부와 메모는 날짜만으로 정해진다 |
| `attendance_tbl` | ✅ | ✅ | ✅ | 후보 키는 `attendance_id`와 `(member_id, attend_date)`. 이름·수업 시각 같은 다른 엔티티의 값을 두지 않는다 |
| `import_log_tbl` | ✅ | ✅ | ✅ | 모든 열이 가져오기 1회를 설명한다. 실행한 회원은 FK로만 가리킨다 |

### 정규화와 헷갈리기 쉬운 것 — 일부러 이렇게 둔 것

- **`attendance_tbl.status`를 저장한다.** 출석·지각·조퇴는 입실·퇴실 시각과 과정 시각으로 판정할 수 있다. 그래도 결석·공결·외출, 고용24에서 가져온 상태는 시각으로 정할 수 없다. 또 과정 시각을 나중에 바꿔도 **그날 판정한 결과**는 그대로 남아야 한다. 그래서 상태는 계산값이 아니라 기록이다. 3NF 위반이 아니다. 같은 날 지각과 조퇴가 모두 인정되면 상태 열을 둘로 나누지 않고 `LATE_EARLY_LEAVE` 값 하나로 기록한다 (1NF 유지, 10/26 결정).
- **`ENUM`(상태, 권한, 출처)을 쓴다.** 값 하나만 들어가므로 1NF를 지킨다. 값에 설명(한글 이름 등)을 붙여 DB에서 관리하려면 코드 테이블(`status_tbl`)로 뗄 수 있다. v0.1에서는 Java `enum`이 이름을 가진다.
- **뷰(`v_monthly_attendance`)는 계산 결과다.** 그래도 저장하지 않고 조회할 때마다 계산하므로 중복도 이상도 없다.
- **`import_log_tbl.row_count`를 저장한다.** 가져온 출결 행이 가져오기 기록을 가리키지 않으므로 나중에 개수를 다시 구할 방법이 없다. 그때 몇 행이었는지를 남기는 기록이다.

## 5. 제약조건과 인덱스

PK는 이름을 붙이지 않는다. MySQL은 PK 이름을 항상 `PRIMARY`로 고정한다. 나머지 제약은 `종류_테이블_내용` 형식으로 이름을 붙인다 ([PLAN.md 7절](PLAN.md#7-데이터-설계)). 어기면 MySQL이 아래 오류 코드를 낸다. Java에서는 `SQLException.getErrorCode()`로 구분한다.

| 이름 | 테이블 | 종류 | 내용 | 어기면 |
| --- | --- | --- | --- | --- |
| `PRIMARY` | 모든 테이블 | PK | 각 테이블의 `..._id`, `class_day_tbl`은 `class_date` | 1062. L-04에서 같은 날짜를 두 번 넣으면 여기서 걸린다 → 트랜잭션 롤백 |
| `uq_member_tbl_phone` | `member_tbl` | UNIQUE | `phone` — 로그인 키 | 1062 → "이미 등록된 휴대폰 번호입니다" |
| `ck_member_tbl_phone` | `member_tbl` | CHECK | `phone REGEXP '^010-[0-9]{4}-[0-9]{4}$'` | 3819 → "010-1234-5678 형식으로 입력해 주세요" |
| `ck_course_tbl_period` | `course_tbl` | CHECK | `end_date >= start_date` | 3819 |
| `fk_attendance_tbl_member_tbl` | `attendance_tbl` | FK | `member_id` → `member_tbl`, ON DELETE CASCADE | 1452 (없는 회원) |
| `fk_attendance_tbl_class_day_tbl` | `attendance_tbl` | FK | `attend_date` → `class_day_tbl` | 1452 (캘린더에 없는 날짜), 1451 (출결이 있는 날짜를 캘린더에서 지움) |
| `uq_attendance_tbl_member_date` | `attendance_tbl` | UNIQUE | `(member_id, attend_date)` — 하루 한 기록 | 1062 → "이미 기록이 있습니다" |
| `ck_attendance_tbl_time` | `attendance_tbl` | CHECK | `check_out IS NULL OR check_in IS NULL OR check_out >= check_in` | 3819 |
| `fk_import_log_tbl_member_tbl` | `import_log_tbl` | FK | `imported_by` → `member_tbl`, ON DELETE CASCADE | 1452 (없는 회원) |
| `idx_attendance_tbl_date` | `attendance_tbl` | INDEX | `attend_date` | — (날짜 범위 조회를 빠르게: L-09, 7절 쿼리) |

- 1062·1451·1452는 `SQLIntegrityConstraintViolationException`으로 온다. 3819(CHECK)는 SQLState가 `HY000`이라 그냥 `SQLException`으로 온다.
- **전화번호 정규식** `^010-[0-9]{4}-[0-9]{4}$`: `^`와 `$`는 문자열의 처음과 끝이다. 그래서 앞뒤에 다른 글자가 붙으면 안 된다. `[0-9]{4}`는 숫자 4개다. `01012345678`(하이픈 없음), `010-123-5678`(가운데 3자리), `011-…`은 막힌다. **Java도 같은 정규식으로 먼저 확인**해 다시 입력받는다. DB `CHECK`는 마지막 안전장치다.
- **나이는 Java가 확인한다.** 스켈레톤처럼 회원가입에서 1 이상인지 보고, DB 제약은 두지 않는다.
- **이름은 안내만 한다.** 회원가입에서 "성과 이름을 띄어쓰기 없이 입력해 주세요"를 보여 준다. DB 제약은 없다 ([8절](#8-남은-질문) 4번).
- `NOT NULL`과 `DEFAULT`는 MySQL에서 이름을 붙일 수 없어서 열 옆에 쓴다. ERD에서는 각 열의 설명(따옴표 안)에 적었다.
- **FK는 수업일인지까지 확인하지 않는다.** 공휴일·휴강일도 `class_day_tbl`에 `is_class_day = FALSE`로 들어 있으므로 FK를 통과한다. "수업일이 아닙니다"(L-05의 3a)는 Java가 INSERT 전에 확인해야 한다.

## 6. 뷰 — `v_monthly_attendance`

훈련생별·월별로 상태마다 몇 번인지 개수를 구한다. **SQL은 상태별 개수만 구하고, 출석률 계산은 Java(`AttendanceRule`)가 한다.**

```mermaid
erDiagram
    member_tbl     ||..o{ v_monthly_attendance : "이름을 가져온다"
    attendance_tbl }|..|| v_monthly_attendance : "한 달 치를 1행으로 묶는다"

    v_monthly_attendance {
        INT         member_id   "member_id + ym 이 한 행"
        VARCHAR(30) name        "member_tbl.name"
        CHAR(7)     ym          "YYYY-MM"
        INT         record_cnt  "기록 일수 (행 수)"
        INT         present_cnt "PRESENT 횟수"
        INT         late_cnt    "LATE + LATE_EARLY_LEAVE 횟수"
        INT         early_cnt   "EARLY_LEAVE + LATE_EARLY_LEAVE 횟수"
        INT         outing_cnt  "OUTING 횟수"
        INT         absent_cnt  "ABSENT 횟수"
        INT         excused_cnt "EXCUSED 횟수"
    }
```

```sql
CREATE OR REPLACE VIEW v_monthly_attendance AS
SELECT m.member_id,
       m.name,
       DATE_FORMAT(a.attend_date, '%Y-%m')                       AS ym,
       COUNT(*)                                                   AS record_cnt,
       SUM(a.status = 'PRESENT')                                  AS present_cnt,
       SUM(a.status IN ('LATE', 'LATE_EARLY_LEAVE'))              AS late_cnt,
       SUM(a.status IN ('EARLY_LEAVE', 'LATE_EARLY_LEAVE'))       AS early_cnt,
       SUM(a.status = 'OUTING')                                   AS outing_cnt,
       SUM(a.status = 'ABSENT')                                   AS absent_cnt,
       SUM(a.status = 'EXCUSED')                                  AS excused_cnt
FROM member_tbl m
JOIN attendance_tbl a ON a.member_id = m.member_id
GROUP BY m.member_id, m.name, DATE_FORMAT(a.attend_date, '%Y-%m');
```

### 뷰 읽는 법

- 뷰는 테이블이 아니다. 데이터를 따로 저장하지 않고, 조회할 때마다 위 `SELECT`를 실행한다. 그래서 PK·FK가 없다. ERD에서는 "어느 테이블을 읽는지"만 점선으로 보여 준다. 식별·비식별 관계가 아니라 **읽는 관계**다.
- `SUM(a.status = 'PRESENT')`: MySQL에서 비교식은 참이면 1, 거짓이면 0이다. 그래서 `SUM`이 곧 "PRESENT인 행의 수"가 된다.
- `SUM(a.status IN ('LATE', 'LATE_EARLY_LEAVE'))`: 지각·조퇴(`LATE_EARLY_LEAVE`)인 날은 `late_cnt`와 `early_cnt`에 **모두 포함**한다. 그래서 지각·조퇴·외출 합계(`late_cnt + early_cnt + outing_cnt`)에 그날이 2회로 들어가 PLAN 3절 환산과 맞는다.
- **기록 일수는 `record_cnt`(`COUNT(*)`)를 쓴다.** 지각·조퇴인 날이 두 개수에 모두 포함되므로 `*_cnt` 컬럼을 더하면 기록 일수보다 커진다.
- **`JOIN`(INNER JOIN)이라서 그 달 출결이 한 건도 없는 훈련생은 뷰에 나오지 않는다.** 그런 훈련생은 7절 쿼리로 따로 찾는다.

## 7. 그 달 기록이 없는 훈련생 찾기 — LEFT JOIN + IS NULL

L-09 반 전체 현황의 "기록 없음" 명단이다 (`AttendanceRepository.findMembersWithoutRecord`).

```sql
SELECT m.member_id, m.name
FROM member_tbl m
LEFT JOIN attendance_tbl a
       ON a.member_id = m.member_id
      AND a.attend_date BETWEEN ? AND ?          -- 그 달 1일 ~ min(말일, 어제)
WHERE a.attendance_id IS NULL;
```

**어떻게 동작하나** — 회원 3명 중 YI만 10월 기록이 없을 때:

| 단계 | 결과 |
| --- | --- |
| ① `LEFT JOIN` | 회원은 **모두** 남는다. 짝이 되는 출결이 없는 YI의 행은 `a.*`가 전부 NULL이다 |
| ② `WHERE a.attendance_id IS NULL` | 출결 짝이 없는 행만 남긴다 → YI |

| member_id | name | a.attendance_id (①) | ② 후 |
| --- | --- | --- | --- |
| 1 | RY | 11, 14, … | 빠짐 |
| 2 | YY | 12, 15, … | 빠짐 |
| 3 | YI | NULL | **남음** |

**날짜 조건을 `WHERE`가 아니라 `ON`에 두는 이유:** `WHERE`로 옮기면 YI 행의 `a.attend_date`가 NULL이라 `BETWEEN`이 참이 되지 못한다. 그러면 YI가 빠지고 결과가 늘 비게 된다. `ON`에 두면 "그 기간의 출결과만 짝을 지어 보고, 짝이 없으면 NULL로 남긴다"가 된다.

`?` 두 개는 `PreparedStatement`로 넣는다. 첫째는 그 달 1일이고, 둘째는 그 달 말일과 어제 중 이른 날이다. 계산 기준일이 어제이기 때문이다 ([PLAN.md 4절 공통 규칙](PLAN.md#공통-규칙)).

## 8. 남은 질문

10/22 ERD 회의는 로그인 방식과 과정–회원 관계를 정했다. 아래는 회의에서 따로 정하지 않은 것이다. 지금 스키마는 각 항목의 **현재** 상태대로 두고, 구현하면서 필요하면 정한다. 바꾸면 같은 PR에서 PLAN.md 7절도 고친다.

1. **출결을 식별 관계로 바꿀지.** 현재: 비식별 + 대리 키 ([3절 대안](#식별-관계로-바꾼다면-대안)).
2. **뷰는 날짜를 거르지 않는다.** 오늘 입력한 출결도 이번 달 횟수에 들어간다. "어제 기준" 계산에 뷰를 쓸 때 오늘 기록을 빼야 하는지 L-09 담당(A)과 출결 규칙 담당(B)이 맞춘다.
3. **[PLAN.md 3절](PLAN.md#3-출결-규칙-코드의-심장)의 `unit_period_days`(단위기간) 설정 컬럼이 `course_tbl`에 없다.** 열을 추가할지, 3절에서 "월 단위 고정"으로 정리할지 정한다.
4. **이름 띄어쓰기는 안내만 하고 막지 않는다.** 막으려면 Java에서 공백을 확인하거나 `CHECK (name NOT LIKE '% %')`를 둔다.
5. **`phone`은 형식이 13자로 고정됐다.** 현재: `VARCHAR(20)`. `CHAR(13)`으로 줄여도 `CHECK`가 형식을 지키므로 동작은 같다.
6. **확장 (v0.1 범위 밖).** 비밀번호 로그인, 여러 과정(회원–과정 관계, 과정별 수업일)은 확장안 [erd-extended.md](erd-extended.md)에서 시작한다.

---

## 부록: Mermaid erDiagram 문법

이 문서의 그림은 모두 Mermaid로 그렸다. GitHub와 노션은 ` ```mermaid ` 코드 블록을 그림으로 보여 준다. VS Code에서는 "Markdown Preview Mermaid Support" 확장을 깔면 미리보기에 나온다. 바로 고쳐 보려면 [mermaid.live](https://mermaid.live)에 붙여 넣는다.

### 기본 모양

````text
```mermaid
erDiagram
    parent_tbl ||..o{ child_tbl : "관계 이름"

    parent_tbl {
        INT parent_id PK "설명"
    }
    child_tbl {
        INT child_id  PK
        INT parent_id FK
    }
```
````

- 첫 줄은 항상 `erDiagram`이다.
- **엔티티 이름은 영문·숫자·`_`로 쓴다.** 한글 이름은 따옴표 없이 쓰면 문법 오류가 난다. 한글 뜻은 관계 이름이나 열 설명에 적는다.
- **관계 한 줄** = `왼쪽엔티티 [왼쪽 기호][선][오른쪽 기호] 오른쪽엔티티 : "이름"`.
- **엔티티 블록** = `{ }` 안에 열을 한 줄에 하나씩 쓴다. 관계가 없는 테이블(`course_tbl`)도 블록만 쓰면 그려진다.
- `%%`로 시작하는 줄은 주석이다.

### 카디널리티 기호 — 선 끝에 붙는 "몇 개"

기호는 **선에서 가까운 쪽이 최소, 엔티티에 가까운 쪽이 최대**다. `|`는 1, `o`는 0, `{` `}`는 여러 개다.

| 왼쪽에 쓸 때 | 오른쪽에 쓸 때 | 뜻 | 이 ERD의 예 |
| --- | --- | --- | --- |
| `\|o` | `o\|` | 0 또는 1 | `import_log_tbl }o..o\| member_tbl` — `imported_by`가 NULL일 수 있다 |
| `\|\|` | `\|\|` | 정확히 1 | `member_tbl \|\|..o{ attendance_tbl` — 출결은 반드시 회원 1명의 것 |
| `}o` | `o{` | 0 이상 | 회원은 출결이 0건일 수도 있다 |
| `}\|` | `\|{` | 1 이상 | `attendance_tbl }\|..\|\| v_monthly_attendance` — 뷰 1행은 출결 1건 이상에서 나온다 |

**읽는 순서:** `member_tbl ||..o{ attendance_tbl`은 양쪽에서 읽는다.

- 오른쪽 기호 `o{`: 회원 1명은 출결을 **0개 이상** 가진다.
- 왼쪽 기호 `||`: 출결 1건은 회원을 **정확히 1명** 가진다.

### 선 모양 — 식별 관계와 비식별 관계

| 선 | 이름 | 뜻 | 이 문서의 예 |
| --- | --- | --- | --- |
| `--` (실선) | 식별 관계 | 부모 키가 자식 **PK의 일부**가 된다 | [3절 대안](#식별-관계로-바꾼다면-대안) |
| `..` (점선) | 비식별 관계 | 부모 키가 자식의 **일반 열(FK)** 로만 들어간다 | 1절의 모든 관계 |

### 열 한 줄 — `타입 이름 키 "설명"`

```text
VARCHAR(20) phone UK "NOT NULL, 로그인 키"
```

| 자리 | 쓰는 것 | 규칙 |
| --- | --- | --- |
| 타입 | `INT`, `VARCHAR(20)`, `DATE` … | 괄호는 되지만 **쉼표와 따옴표는 안 된다.** 그래서 `ENUM('ADMIN','TRAINEE')`는 `ENUM`으로만 쓰고 값은 설명에 적는다 |
| 이름 | 열 이름 | 공백 없이 |
| 키 (선택) | `PK`, `FK`, `UK` | 여러 개면 쉼표로: `FK, UK`, `PK, FK`. `UK`는 UNIQUE |
| 설명 (선택) | `"..."` | 큰따옴표 안에 자유롭게. NOT NULL, 기본값, 뜻을 적는다. 큰따옴표 자체는 쓸 수 없다 |

### 배치 — 표의 위치는 직접 정할 수 없다

Mermaid는 표를 자동으로 배치한다. 위치를 좌표로 정할 수는 없고, 두 가지로 방향만 바꾼다.

1. **배치 방향:** 맨 위에 설정을 둔다. 기본은 위 → 아래(`TB`)이고, `LR`이면 왼쪽 → 오른쪽으로 놓는다.

   ```text
   ---
   config:
     er:
       layoutDirection: LR
   ---
   erDiagram
   ```

2. **관계를 쓰는 방향:** 관계 한 줄의 **왼쪽 엔티티가 앞(왼쪽 또는 위)** 에 놓인다. 1절은 이렇게 썼다.
   - `import_log_tbl }o..o| member_tbl` → 가져오기 기록이 회원 앞(왼쪽)
   - `member_tbl ||..o{ attendance_tbl`, `class_day_tbl ||..o{ attendance_tbl` → 두 차원이 같은 열에 위아래로, 팩트는 그 오른쪽
   - 순서를 뒤집어 쓸 때는 기호도 함께 뒤집으므로 뜻은 같다: `class_day_tbl ||..o{ attendance_tbl` = `attendance_tbl }o..|| class_day_tbl`.

#### 할 수 없는 것

- 관계로 이어진 두 표는 **같은 열(LR) 또는 같은 줄(TB)에 놓을 수 없다.** 그래서 팩트와 이어진 뷰를 팩트 **아래**에 둘 수 없다. 관계 줄 순서 120가지를 모두 렌더링해 봐도 차원을 왼쪽에, 뷰를 팩트 아래에 두는 순서는 없었다 (Mermaid 11.4.1).
- 관계가 없는 표(`course_tbl`)의 자리는 정할 수 없다.
- 정확한 배치가 필요하면 좌표를 직접 정하는 도구(예: [erd-extended.excalidraw](erd-extended.excalidraw)를 그린 Excalidraw)로 그린다.

`erDiagram` 아래에 `direction LR`을 쓰는 방법은 Mermaid 버전에 따라 지원되지 않는다. 지원되지 않는 버전에서는 `direction`과 `LR`이 표로 그려지니 쓰지 않는다.

### Mermaid로 나타낼 수 없는 것

- **NOT NULL, DEFAULT, CHECK, 복합 UNIQUE, INDEX, ON DELETE** → 열 설명에 적거나 이 문서의 표(3절, 5절)에 적는다.
- **뷰** → 엔티티로 그리고 원본 테이블과 점선으로 잇는다 (6절). 뷰라는 표시는 이름의 `v_` 접두사와 설명으로 한다.
