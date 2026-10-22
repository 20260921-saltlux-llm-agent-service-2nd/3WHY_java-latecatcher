# GIT 컨벤션 — java-latecatcher (팀 3WHY) 팀 규칙

> 팀 3WHY (3명). 각자 기능을 나눠 개발하고, PR을 올리고, **리뷰는 함께** 한다.
> 기능 목록과 기능 번호는 [PLAN.md 4절](PLAN.md#4-기능), 역할 분담은 [PLAN.md 11절](PLAN.md#11-팀-구성과-역할-분담-3명)과 [15절](PLAN.md#15-업무-분담--기능--문서-v01)에 있다.
>
> 이 문서의 예시에 나오는 이슈·PR 번호(#24 이후)는 **설명용**이다. 실제 번호는 GitHub가 붙인다.

---

## 한눈에 보기

기능 하나(**L-05 출결 입력**)가 이슈부터 머지까지 가는 동안 번호가 어디에 붙는지 보여 준다.

| 단계 | 형식 | 예 |
| --- | --- | --- |
| 기능 | PLAN.md 4절의 기능 번호 | `L-05` 출결 입력 |
| 이슈 | `[기능번호] 제목` | `[L-05] 출결 입력 — 입실·퇴실 시각으로 상태 판정` → GitHub가 **#24** 부여 |
| 라벨 | 종류 1개 + 파트 1개 이상 | `✨ Feature` `rule` `controller` |
| 브랜치 | `종류/이슈번호-기능번호-영문이름` | `feat/24-L05-attendance-checkin` |
| 커밋 | `태그: 제목` + body + `Refs #이슈번호` | `feat: 입실 시각으로 지각 여부 판정` … `Refs #24` |
| **main pull** | push 전에 최신 `main`을 당겨 충돌·빌드 확인 | `git pull origin main` → `git status` → `./gradlew compileJava` |
| push | 충돌이 없을 때만 | `git push -u origin feat/24-L05-attendance-checkin` |
| PR | `태그: [기능번호] 제목 #이슈번호` | `feat: [L-05] 출결 입력과 상태 판정 #24` → GitHub가 **#27** 부여 |
| PR 본문 | `Closed #이슈번호` | `Closed #24` |
| 리뷰 | approve 2 | 작성자를 뺀 두 명 모두 |
| 머지 커밋 | PR 제목 + 변경 사항 + `Closed #이슈번호` | `feat: [L-05] 출결 입력과 상태 판정 #24` … `Closed #24` |
| 결과 | 이슈 자동 close | #24 닫힘 |

전체 과정을 명령어와 함께 따라가 보려면 [8절](#8-전체-예시--l-05-출결-입력을-처음부터-끝까지)을 본다.

---

## 0. 번호 체계 — 기능 번호와 이슈 번호

이 프로젝트에는 번호가 세 가지 있다. 섞어 쓰면 "4번"이 기능인지 이슈인지 PR인지 알 수 없다.

| 번호 | 누가 정하나 | 모양 | 특징 |
| --- | --- | --- | --- |
| **기능 번호** | 팀 ([PLAN.md 4절](PLAN.md#4-기능)) | `L-05` | 프로젝트 내내 바뀌지 않는다. **`#`을 붙이지 않는다** |
| **이슈 번호** | GitHub 자동 | `#24` | 이슈를 만들 때 정해진다 |
| **PR 번호** | GitHub 자동 | `#27` | **이슈와 같은 번호 카운터를 쓴다.** 그래서 PR 번호는 이슈 번호와 다르다 |

### 규칙

1. **기능 번호 앞에 `#`을 붙이지 않는다.** `#`은 GitHub에서 이슈·PR 링크 기호다. `#F-04`처럼 쓰면 이슈 4번으로 읽힌다.
2. **한 기능에 이슈가 여러 개일 수 있다.** 기능을 쪼개거나, 머지 뒤에 버그가 나오면 기능 번호는 같고 이슈 번호만 다르다.
3. **기능 번호가 없는 일**은 대괄호에 영역을 쓴다.

   | 영역 | 언제 |
   | --- | --- |
   | `[환경]` | 0단계 세팅, 패키지 이름, `build.gradle`, `.gitignore` |
   | `[문서]` | README, PLAN, CONVENTION, 노션 페이지 |
   | `[논의]` | 팀이 정할 주제 (규칙 수치, 머지 방식 등) |

4. **스켈레톤 단계 이슈(F-01 ~ F-11, #5 ~ #20)는 제목을 고치지 않는다.** 새로 만드는 이슈부터 이 규칙을 따른다. 스켈레톤 기능과 L 번호의 대응은 PLAN.md 4절 표의 "스켈레톤" 컬럼에 있다.

### 번호가 들어가는 곳

| 어디 | 기능 번호 | 이슈 번호 | 예 |
| --- | :---: | :---: | --- |
| 이슈 제목 | ✓ | (자동) | `[L-05] 출결 입력 — 입실·퇴실 시각으로 상태 판정` |
| 브랜치 | ✓ (`L05`) | ✓ | `feat/24-L05-attendance-checkin` |
| 커밋 header | — | — | `feat: 입실 시각으로 지각 여부 판정` |
| 커밋 마지막 줄 | — | ✓ | `Refs #24` |
| PR 제목 | ✓ | ✓ | `feat: [L-05] 출결 입력과 상태 판정 #24` |
| PR 본문 | — | ✓ | `Closed #24` |
| 머지 커밋 | ✓ | ✓ | PR 제목 그대로 + `Closed #24` |

커밋 header에는 번호를 넣지 않는다. 커밋은 작게 여러 개 만들기 때문에 매번 번호를 쓰면 header가 길어진다. 대신 마지막 줄의 `Refs #24`가 이슈 #24의 타임라인에 커밋을 자동으로 연결한다.

---

## 1. Commit Rule

- 설명은 **한글**로 쓴다.
- 하나의 커밋 = 하나의 변경. 여러 기능을 한 커밋에 섞지 않는다.
- **header만 적지 말고 body까지 꼭 적는다.**
- **마지막 줄에 `Refs #이슈번호`** 를 적는다.

### Header — `태그: 제목`

`:` 뒤에만 공백이 있다. 번호와 탭 문자는 넣지 않는다.

| 태그 | 언제 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 (삭제 포함) |
| `docs` | 문서 수정 (README, PLAN, 주석) |
| `refactor` | 동작은 같고 구조·이름만 바꿀 때 |
| `test` | 테스트 추가·수정 |
| `chore` | 빌드 설정, `.gitignore`, 템플릿 등 코드 외 작업 |

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 | ✓ 고친 예 |
| --- | --- | --- |
| `feat: F-04(탭)신규 구독 추가` | header에 기능 번호와 탭 문자 | `feat: 신규 구독 추가 구현` |
| `feat: 내 현재 구독 정보 조회 구현 - 로그인한 회원의 ID로 … - ID, OTT타입 …` | body를 header 한 줄에 이어 씀 | header 한 줄 + 빈 줄 + `-` 목록 body |
| `feat:로그인`, `feat : 로그인` | `:` 앞뒤 공백 | `feat: 로그인` |

### Body

- 양에 구애받지 말고 **최대한 상세히** 쓴다.
- **어떻게**보다 **무엇을, 왜** 바꿨는지 쓴다.
- header 다음에 한 줄 띄우고 `-`로 시작하는 목록으로 쓴다.
- body 다음에 한 줄 띄우고 `Refs #이슈번호`로 끝낸다.

```text
feat: 입실 시각으로 지각 여부 판정

- course_tbl의 start_time을 기준으로, 입실 시각이 그보다 늦으면 LATE,
  같거나 이르면 PRESENT로 판정한다.
- 판정은 AttendanceRule.judge()에 두어 DB 없이 JUnit으로 테스트할 수 있게 했다.
- 09:00:00 정각 입실이 PRESENT인지 경계값 테스트를 추가했다.

Refs #24
```

```text
fix: 로그인 성공 시 true를 반환하도록 수정

- 성공 분기에 return이 없어 컴파일되지 않았다.
- null 검사 대상이 memberRepository여서 미등록 번호도 로그인됐다.
  조회 결과인 loginMember를 검사하도록 바꿨다.

Refs #31
```

> `git pull origin main`으로 생기는 `Merge branch 'main' …` 커밋은 기본 메시지 그대로 둔다.

---

## 2. Branch Rule

### 이름 — `종류/이슈번호-기능번호-영문이름`

| 부분 | 규칙 | 예 |
| --- | --- | --- |
| 종류 | 커밋 태그와 같은 단어: `feat`, `fix`, `refactor`, `test`, `docs`, `chore` | `feat` |
| 이슈번호 | 숫자만. **`#`을 붙이지 않는다** | `24` |
| 기능번호 | `L` + 두 자리 숫자, 하이픈 없이. 기능 번호가 없는 일은 생략 | `L05` |
| 영문이름 | PLAN.md 4절의 "영문 이름(브랜치)". 없으면 영어 단어 2~3개 | `attendance-checkin` |

전부 **소문자**와 하이픈(`-`)만 쓴다. 기능번호의 `L`만 대문자다. 한글과 공백은 쓰지 않는다.

```text
feat/24-L05-attendance-checkin        # 기능
feat/25-L06-import-read-preview          # 한 기능을 쪼갠 첫 번째 이슈
feat/26-L06-import-transaction-save      # 같은 기능의 두 번째 이슈
fix/31-L02-login-return               # 머지 뒤 발견한 버그
test/33-L05-late-boundary-test        # 테스트만 추가
chore/30-package-rename               # [환경] — 기능 번호 없음
docs/35-notion-requirements           # [문서] — 기능 번호 없음
```

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 |
| --- | --- |
| `Feat/#20-f-04-AddMember-2` | 대문자 종류, `#`, 스켈레톤 번호, CamelCase |
| `20-feat-f-04-AddMember-2` | 종류가 앞에 없음. 위 브랜치와 같은 작업이 이름만 다르게 두 개 |
| `feat/7-f-01-회원가입` | 한글. PR 제목이 `Feat/7 f 01 회원가입`으로 자동 생성됨 |
| `feat/#11-…` 와 `feature/#11-…` | 같은 이슈에 종류만 다른 브랜치 두 개 |

**`#`을 빼는 이유:** URL에서 `#`은 페이지 안 위치를 뜻한다. 그래서 `github.com/…/tree/feat/#24-…` 링크가 브랜치를 열지 못한다. 일부 셸 설정에서는 `#`이 든 브랜치 이름을 따옴표로 감싸야 한다.

### 흐름 — `기능 브랜치` → `main`

- `main`에는 **정말 필요하고 안전한 코드만** 있어야 한다.
- `main`에 직접 push하지 않는다. (브랜치 보호 설정으로 막아 둔다)
- 브랜치는 항상 **최신 `main`에서** 딴다. 작업이 길어지면 `main`을 자주 당겨온다.
- **push하기 전에 반드시 최신 `main`을 pull해서 충돌이 없는지 확인한다.** 아래 순서를 지킨다.
- 이슈 하나에 브랜치 하나. 이름을 잘못 지었으면 새로 만들지 말고 바꾼다 (`git branch -m 새이름`).
- 머지된 브랜치는 지운다.

### 작업 순서 — 구현 → 커밋 → main pull → 충돌 확인 → push

| 순서 | 할 일 | 명령 |
| --- | --- | --- |
| 1 | 최신 `main`에서 브랜치를 딴다 | `git switch main` → `git pull origin main` → `git switch -c 브랜치이름` |
| 2 | 구현하고 커밋한다 | `git add 파일` → `git commit` |
| 3 | **최신 `main`을 내 브랜치로 pull한다** | `git pull origin main` |
| 4 | **충돌이 없는지 확인한다** | `git status`에 `both modified`가 없어야 한다 |
| 5 | 충돌이 있으면 해결하고 커밋한다 | 파일의 `<<<<<<<` ~ `>>>>>>>` 구간 정리 → `git add 파일` → `git commit` |
| 6 | **빌드가 되는지 확인한다** | `./gradlew compileJava` (테스트가 있으면 `./gradlew test`) |
| 7 | 그 다음에 push한다 | `git push -u origin 브랜치이름` |

```bash
# 1. 브랜치 만들기
git switch main
git pull origin main
git switch -c feat/24-L05-attendance-checkin

# 2. 구현, 커밋
git add src/main/java/com/latecatcher/app/rule/AttendanceRule.java
git commit

# 3. push 전에 최신 main 당겨오기
git pull origin main

# 4. 충돌 확인 — "both modified"가 보이면 5번으로
git status

# 6. 빌드 확인
./gradlew compileJava

# 7. push
git push -u origin feat/24-L05-attendance-checkin
```

### 충돌이 났을 때

```text
$ git pull origin main
CONFLICT (content): Merge conflict in src/main/java/com/latecatcher/app/repository/AttendanceRepository.java
Automatic merge failed; fix conflicts and then commit the result.
```

1. 충돌난 파일을 열어 `<<<<<<< HEAD`(내 코드)와 `>>>>>>> origin/main`(main 코드) 사이를 정리한다. 두 코드가 모두 필요하면 둘 다 남긴다.
2. 다른 팀원의 코드를 지워야 할 것 같으면 **혼자 결정하지 말고 그 코드의 작성자에게 먼저 묻는다.**
3. `git add 파일` → `git commit` (메시지는 기본값 `Merge branch 'main' …` 그대로)
4. 빌드를 다시 확인한 뒤 push한다.
5. PR `참고 사항`에 "main과 충돌 — `AttendanceRepository` 메서드 순서 정리"처럼 한 줄 남긴다.

> PR을 연 뒤에 `main`이 바뀌어도 같은 순서를 반복한다. 머지 직전에 GitHub가 "This branch is out-of-date"를 보여 주면 3 ~ 7번을 다시 하고 push한다.

---

## 3. Label Rule

라벨은 **이슈와 PR**에 붙인다. 브랜치와 커밋에는 라벨이 없다.
대신 **종류 라벨 하나가 이슈 템플릿, 이슈 제목 앞머리, 브랜치 종류, 커밋·PR 태그를 함께 정한다.** 라벨을 먼저 고르면 나머지 이름이 따라온다.

### 라벨 세 묶음

| 묶음 | 몇 개 붙이나 | 무엇을 나타내나 |
| --- | --- | --- |
| **종류** | 이슈·PR마다 **정확히 1개** | 무슨 일인가 (기능, 버그, 리팩터링 …) |
| **파트** | **1개 이상** | 어느 패키지·파일을 바꾸나 |
| **상태** | 필요할 때만 | 머지된 기능을 다시 고치는 중인가 (`수정(fix)`) |

담당자는 라벨로 표시하지 않는다. 이슈와 PR의 **Assignees**에 지정한다.

### 종류 라벨 — 이슈·브랜치·커밋·PR 이름을 정한다

| 종류 라벨 | 이슈 템플릿 | 이슈 제목 앞머리 | 브랜치 종류 | 커밋·PR 태그 | 라벨이 생기기 전 임시 라벨 |
| --- | --- | --- | --- | --- | --- |
| `✨ Feature` | `1-feature.md` | `[L-05]` | `feat/` | `feat` | `enhancement` |
| `🐞 Bug` | `2-bug.md` | `[L-02]` (기능 무관하면 `[환경]`) | `fix/` | `fix` | `bug` |
| `🔨 Refactor` | `3-refactor.md` | `[L-06]` (여러 기능에 걸치면 `[환경]`) | `refactor/` | `refactor` | `enhancement` |
| `🌏 Environment` | `4-environment.md` | `[환경]` 또는 `[문서]` | `chore/` (설정) / `docs/` (문서) | `chore` / `docs` | `enhancement` |
| `🧪 Test` | `1-feature.md`로 만들고 라벨만 바꾼다 | `[L-05]` | `test/` | `test` | `enhancement` |
| `💬 Suggestion` | `5-suggestion.md` | `[논의]` | 만들지 않는다 | — | `question` |

`💬 Suggestion` 이슈는 코드를 바꾸지 않는다. 결론이 나서 문서를 고쳐야 하면 `🌏 Environment` 이슈를 새로 만든다.

### 파트 라벨 — 바꾸는 파일 위치로 고른다

경로는 `src/main/java/com/latecatcher/app/` 기준이다 ([PLAN.md 9절](PLAN.md#9-프로젝트-구조)).

| 파트 라벨 | 바꾸는 파일 | 예 |
| --- | --- | --- |
| `common` | `common/` | `JDBCTemplate` |
| `config` | `build.gradle`, `.gitignore`, `resources/db.properties.example`, `.github/` | mainClass, 이슈 템플릿 |
| `sql` | `resources/db/*.sql`, `mapper/*.xml` | `schema.sql`, `attendance-query.xml` |
| `model` | `model/` | `Attendance`, `AttendanceStatus` |
| `rule` | `rule/`, `src/test/…/rule/` | `AttendanceRule`, `AttendanceRuleTest` |
| `importer` | `importer/` | `Work24Importer` |
| `repository` | `repository/` | `AttendanceRepository` |
| `controller` | `controller/` | `AttendanceController` |
| `view` | `view/` | `AttendanceView` |
| `run` | `run/` | `Application` 메뉴 |
| `docs` | `README.md`, `docs/`, 노션 페이지 | PLAN, CONVENTION |

바꾸는 패키지마다 하나씩 붙인다. 리뷰어는 파트 라벨을 보고 자기가 맡은 파일이 바뀌는지 바로 알 수 있다.

### 상태 라벨

| 상태 라벨 | 언제 | 함께 붙이는 종류 라벨 |
| --- | --- | --- |
| `수정(fix)` | **이미 main에 머지된 기능**을 다시 고칠 때 (저장소에 이미 있음) | `🐞 Bug` 또는 `🔨 Refactor` |

### 붙이는 규칙

1. **이슈:** 템플릿이 종류 라벨을 자동으로 붙인다. 작성자는 **파트 라벨을 직접 붙인다.**
2. **브랜치:** 라벨은 없다. 이름의 종류(`feat/`, `fix/` …)는 위 종류 라벨 표를 따른다.
3. **PR:** 이슈의 라벨을 **그대로** 붙인다. 작업하면서 이슈에 없던 패키지를 더 바꿨다면 그 파트 라벨을 PR에 추가한다.
4. **종류가 바뀌면** (기능인 줄 알았는데 버그였다) 이슈 라벨을 바꾸고, 브랜치 이름도 `git branch -m`으로 맞춘다.
5. **기존 라벨:** 스켈레톤 단계 이슈(#5 ~ #20)의 `enhancement` 라벨은 그대로 둔다. GitHub 기본 라벨은 지우지 않고, 새 라벨이 생긴 뒤에는 새 이슈에 쓰지 않는다.

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 | ✓ 고친 예 |
| --- | --- | --- |
| `#20 [FEAT] #F-04: 신규 구독 추가(2)` — `수정(fix)`만 | 종류 라벨이 없음 | `🐞 Bug`, `수정(fix)`, `controller` |
| `#5 ~ #15` — `enhancement`만 | 파트 라벨이 없어 어느 파일을 바꾸는지 모름 | `✨ Feature`, `repository`, `controller` |
| `#1 MD 파일 수정` — 라벨 없음 | 종류·파트 모두 없음 | `🌏 Environment`, `docs` |

### 예시 — 상황별 라벨과 이름

| 상황 | 이슈 제목 | 라벨 | 브랜치 | PR 제목 |
| --- | --- | --- | --- | --- |
| 기능 구현 | `[L-05] 출결 입력 — 입실·퇴실 시각으로 상태 판정` | `✨ Feature` `rule` `controller` | `feat/24-L05-attendance-checkin` | `feat: [L-05] 출결 입력과 상태 판정 #24` |
| 기능을 쪼갠 두 번째 이슈 | `[L-06] 출결 가져오기 — 트랜잭션 저장` | `✨ Feature` `repository` `sql` | `feat/26-L06-import-transaction-save` | `feat: [L-06] 출결 가져오기 트랜잭션 저장 #26` |
| 머지된 기능의 버그 | `[L-02] 로그인 성공 시 true 미반환, 미등록 번호도 로그인됨` | `🐞 Bug` `수정(fix)` `controller` | `fix/31-L02-login-return` | `fix: [L-02] 로그인 성공 시 true 반환 #31` |
| 테스트만 추가 | `[L-05] 09:00 정각 입실 경계값 테스트` | `🧪 Test` `rule` | `test/33-L05-late-boundary-test` | `test: [L-05] 지각 판정 경계값 테스트 #33` |
| 리팩터링 | `[L-06] Work24Importer 파싱을 스트림으로 정리` | `🔨 Refactor` `importer` | `refactor/40-L06-import-stream-parse` | `refactor: [L-06] Work24Importer 스트림 파싱 #40` |
| 환경 설정 | `[환경] 패키지 이름 skeleton → com.latecatcher.app 변경` | `🌏 Environment` `config` `repository` | `chore/30-package-rename` | `chore: [환경] 패키지 이름 com.latecatcher.app으로 변경 #30` |
| 문서 | `[문서] 노션 요구사항 명세 페이지 작성` | `🌏 Environment` `docs` | `docs/35-notion-requirements` | `docs: [문서] 노션 요구사항 명세 링크 추가 #35` |
| 논의 | `[논의] 지각 환산 단위기간을 월로 할지 30일로 할지` | `💬 Suggestion` `rule` | 만들지 않음 | 없음 |

### 라벨 만들기 (0단계 [환경] 이슈)

저장소 관리 권한이 있는 사람이 한 번 실행한다. `--force`는 같은 이름의 라벨이 있으면 색과 설명만 고친다. `수정(fix)`는 이미 있으므로 건드리지 않는다.

```bash
# 종류 라벨 6개
gh label create "✨ Feature"     --color A2EEEF --description "새 기능 또는 명세 추가" --force
gh label create "🐞 Bug"         --color D73A4A --description "버그" --force
gh label create "🔨 Refactor"    --color FBCA04 --description "동작은 같고 구조·이름만 변경" --force
gh label create "🌏 Environment" --color 0E8A16 --description "환경 설정, 초기 세팅, 문서" --force
gh label create "🧪 Test"        --color 5319E7 --description "테스트 추가" --force
gh label create "💬 Suggestion"  --color D876E3 --description "팀 논의 주제, 제안" --force

# 파트 라벨 11개 — 종류 라벨과 구분되게 한 가지 색
for part in common config sql model rule importer repository controller view run docs; do
  gh label create "$part" --color C5DEF5 --description "파트: $part" --force
done
```

---

## 4. Issue Rule

### 제목 — `[기능번호] 제목`

기능 번호가 없는 일은 `[환경]`, `[문서]`, `[논의]` 중 하나를 쓴다. 제목은 **무엇을 하는지 한 줄로** 드러나게 쓴다. 이슈 종류(기능/버그 등)는 제목이 아니라 **라벨**로 표시한다.

```text
[L-05] 출결 입력 — 입실·퇴실 시각으로 상태 판정
[L-06] 출결 가져오기 — 파일 읽기와 미리보기
[L-06] 출결 가져오기 — 트랜잭션 저장
[L-02] 로그인 성공 시 true 미반환, 미등록 번호도 로그인됨
[환경] 패키지 이름 skeleton → com.latecatcher.app 변경
[문서] 노션 요구사항 명세 페이지 작성
[논의] 지각 환산 단위기간을 월로 할지 30일로 할지
```

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 | ✓ 고친 예 |
| --- | --- | --- |
| `[FEAT] #F-04: 신규 구독 추가(2)` | 기능 번호에 `#`. 종류를 제목에 씀. "(2)"만으로는 무엇이 다른지 모름 | `[L-05] 출결 입력 — 중복 날짜 입력 안내` |
| `[FEAT]  F-01 회원가입`, `[FEAT] F-06: 구독 기간 연장` | 공백 두 칸, `:` 유무가 제각각 | `[L-01] 회원가입` |
| `MD 파일 수정` | 어느 파일을 왜 고치는지 모름 | `[문서] CONVENTION 번호 체계 재작성` |

### 라벨 — 종류 1개 + 파트 1개 이상

고르는 법과 상황별 예시는 [3절](#3-label-rule)에 있다.

### 내용 — 템플릿을 쓴다

`.github/ISSUE_TEMPLATE/`에 종류별 템플릿을 둔다. 모든 이슈의 첫 섹션에 **기능 번호**와 **예상 작업 시간**을 적는다. 예상 시간은 PR에서 실제 시간과 비교한다.

| 템플릿 | 항목 |
| --- | --- |
| ✨ Feature | 📄 Describe (기능 번호, 설명, 예상 시간) / ✅ Tasks (세분화한 할 일 체크리스트) / 🙋🏻 More |
| 🐞 Bug | 🐞 버그 설명 (기능 번호, 예상 시간) / ✅ 예상 결과 / ❗ 실제 결과 / 💻 재현 단계 / 📄 로그 / 🌏 환경 / 🙋🏻 More |
| 🔨 Refactor | 🔨 Describe (왜 리팩터링하나) / ✅ Tasks / 🙋🏻 More |
| 🌏 Environment | 🤔 Describe (현재 상황) / ✅ Tasks / 🙋🏻 More |
| 💬 Suggestion | 💬 주제 / 🤔 선택지 / 🙋🏻 More |

#### Feature 이슈 예

```markdown
제목: [L-06] 출결 가져오기 — 트랜잭션 저장
labels: ✨ Feature, repository, sql

## 📄 Describe
- 기능 번호: L-06 (PLAN.md 4절) / 담당: A
- 예상 작업 시간: 3h

미리보기에서 y를 받은 출결 목록을 하나의 트랜잭션으로 저장한다.
중간 행이 실패하면 한 행도 남지 않게 한다. 파일 읽기와 미리보기는 #25에서 다룬다.

## ✅ Tasks
- [ ] mapper/attendance-query.xml 에 replaceAttendance, insertImportLog 작성
- [ ] AttendanceRepository.saveAll(list, fileName, memberId) — setAutoCommit(false) / commit / rollback
- [ ] import_log_tbl INSERT를 같은 Connection에서 실행
- [ ] 30행째를 고의로 깨뜨려 0행 저장 확인

## 🙋🏻 More
- 관련 #25 (같은 기능의 앞 단계)
- 100행 이상일 때 executeBatch 전환은 #29로 뺐다.
```

#### Bug 이슈 예

```markdown
제목: [L-02] 로그인 성공 시 true 미반환, 미등록 번호도 로그인됨
labels: 🐞 Bug, 수정(fix), controller

## 🐞 버그 설명
- 기능 번호: L-02 (PLAN.md 4절) / 들어온 곳: #19
- 예상 작업 시간: 0.5h

login()의 성공 분기에 return true가 없고, null 검사 대상이 loginMember가 아니라 memberRepository다.

## ✅ 예상 결과
미등록 번호는 "등록되지 않은 휴대폰 번호입니다." 출력 후 false 반환

## ❗ 실제 결과
컴파일 오류. return만 고치면 미등록 번호도 "환영 합니다.null" 출력 후 회원 메뉴 진입

## 💻 재현 단계
1. ./gradlew compileJava → MemberController.java 74행 missing return statement
```

#### 문서 이슈 예

```markdown
제목: [문서] 노션 요구사항 명세 페이지 작성
labels: 🌏 Environment, docs

## 🤔 Describe
- 기능 번호: 없음 (PLAN.md 15절 A 담당 문서)
- 예상 작업 시간: 2h

PLAN.md 4절 기능 표를 바탕으로 기능별 입력, 검증, 출력 메시지를 노션에 정리한다.

## ✅ Tasks
- [ ] L-01 ~ L-12 기능별 입력값과 검증 규칙
- [ ] 성공·실패 출력 메시지
- [ ] 노션 링크를 README에 추가
```

---

## 5. PR Rule

PR 본문은 **`.github/PULL_REQUEST_TEMPLATE.md`가 정본**이다. PR을 열면 자동으로 채워지는 네 항목을 그대로 쓰고, 항목을 추가하거나 지우지 않는다. 템플릿을 바꾸고 싶으면 템플릿 파일을 PR로 고치고, 이 문서를 그에 맞춘다.

1. **이슈를 먼저** 만든다. 무엇을 구현할지, **예상 작업 시간**까지 이슈에 적은 뒤 브랜치를 딴다.
2. PR 제목은 **`태그: [기능번호] 제목 #이슈번호`** 다. 기능 번호가 없으면 `[환경]`, `[문서]`를 쓴다.
3. 라벨은 이슈와 같은 것을 붙인다. 이슈에 없던 패키지를 더 바꿨으면 그 파트 라벨을 추가한다 ([3절](#3-label-rule)).
4. PR 하나는 **동작하는 기능 하나**. 리뷰어가 20분 안에 읽을 수 있는 크기로 유지한다. 커지면 이슈를 쪼갠다.
5. 머지 방식은 **Merge commit** (squash 아님) — 공들여 쓴 커밋 body를 `main` 히스토리에 남기기 위해서다. *(팀 결정 — [PLAN.md 14절](PLAN.md#14-열린-질문-정해지면-이-문서를-고친다) 6번)*

```text
feat: [L-05] 출결 입력과 상태 판정 #24
feat: [L-06] 출결 가져오기 트랜잭션 저장 #26
fix: [L-02] 로그인 성공 시 true 반환 #31
chore: [환경] 패키지 이름 com.latecatcher.app으로 변경 #30
docs: [문서] 노션 요구사항 명세 링크 추가 #35
```

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 | ✓ 고친 예 |
| --- | --- | --- |
| `Feat/7 f 01 회원가입` | 브랜치 이름으로 자동 생성된 제목을 그대로 둠 | `feat: [L-01] 회원가입 #7` |
| `Feature/#5 add membership` | 같은 문제 | `feat: [L-05] 출결 입력과 상태 판정 #24` |
| `feat: MemberController 로그아웃` | 이슈 번호와 기능 번호가 없음 | `feat: [L-10] 로그아웃 #12` |

> GitHub는 브랜치 이름으로 PR 제목을 자동으로 채운다. **PR을 만들 때 제목을 반드시 고친다.**

### 템플릿 항목별로 쓰는 것

템플릿 원문은 [`.github/PULL_REQUEST_TEMPLATE.md`](../.github/PULL_REQUEST_TEMPLATE.md). 각 항목에 무엇을 쓰는지만 정한다.

| 항목 | 쓰는 것 |
| --- | --- |
| **📌 관련 이슈번호** | `Closed #이슈번호`. 머지되면 이슈가 자동으로 닫힌다. 닫지 않고 참고만 할 이슈는 `관련 #번호`로 따로 적는다 |
| **📌 해결하는 데 얼마나 걸렸나요?** | 이슈에 적은 **예상 시간 대비 실제 시간** (`예상 4h / 실제 6h`). 차이가 크면 왜 그랬는지 한 줄 |
| **📌 PR Point** | ① **변경 사항과 이유** — `-` 목록 (머지 커밋 body가 된다) ② **리뷰어가 집중해서 볼 곳** — 고민한 선택지가 있었다면 왜 이쪽을 택했는지 |
| **📌 참고 사항** | ① **확인 방법** — 사전 준비, 메뉴 번호, 입력값, 기대 출력 ② **실습한 학습 항목** — [PLAN.md 5절](PLAN.md#5-배운-것--이-프로젝트에서-실습하는-것) 표에서 이번 PR이 다룬 것 ③ 후속 이슈 |

#### 작성 예

```markdown
## 📌 관련 이슈번호
* Closed #26
* 관련 #25

## 📌 해결하는 데 얼마나 걸렸나요?  (실제 작업 시간)
예상 3h / 실제 5h — rollback 뒤에도 import_log가 남는 문제를 찾느라 2h 더 걸렸다.

## 📌 PR Point
변경 사항
- 미리보기에서 y를 받은 Attendance 목록을 하나의 트랜잭션으로 저장한다.
- 중간 행이 실패하면 전부 롤백된다.
- 같은 날짜 기록은 REPLACE INTO로 덮어써서 다시 가져올 수 있다.

집중해서 볼 곳
- AttendanceRepository.saveAll() — import_log INSERT를 같은 Connection에서 하는지.
  처음엔 별도 Connection을 써서 rollback이 안 됐다.

## 📌 참고 사항
확인 방법
1. schema.sql 재실행 (import_log_tbl 추가됨)
2. `data/sample.txt`를 `data/broken.txt`로 복사하고, 30번째 날짜 줄의 일자를 수업일이 아닌 `2026-10-03`(토)으로 바꾼다
3. 로그인 → 메뉴 3 → 경로 `data/broken.txt` → y → 저장 중 FK 위반 → "저장 실패, 0행 저장"
4. `select count(*) from attendance_tbl;` → 0 (앞의 29줄도 남지 않음 = 롤백 확인)
5. 메뉴 3 → 경로 `data/sample.txt` → 미리보기 120행 확인 → y → count 120

실습한 학습 항목
- JDBC 트랜잭션 (setAutoCommit / commit / rollback), REPLACE INTO

후속: 100행 이상일 때 executeBatch 전환 → #29
```

### 머지 커밋 메시지

GitHub의 기본 머지 메시지는 `Merge pull request #28 from user/feat/26-L06-import-transaction-save` 다. 이대로 두면 `main` 로그에 태그도 설명도 남지 않으므로 **머지 버튼을 누를 때 메시지를 커밋 규칙에 맞게 고쳐 쓴다.**

| | 규칙 |
| --- | --- |
| **Header** | **PR 제목 그대로.** `태그: [기능번호] 제목 #이슈번호` |
| **Body** | PR Point의 **"변경 사항"** 목록을 그대로 옮긴다 |
| **마지막 줄** | `Closed #이슈번호` |

```text
feat: [L-06] 출결 가져오기 트랜잭션 저장 #26

- 미리보기에서 y를 받은 Attendance 목록을 하나의 트랜잭션으로 저장한다.
- 중간 행이 실패하면 전부 롤백된다.
- 같은 날짜 기록은 REPLACE INTO로 덮어써서 다시 가져올 수 있다.

Closed #26
```

| ✗ 이 저장소에서 나온 예 | 무엇이 문제인가 |
| --- | --- |
| `Merge pull request #19 from , feat/7-f-01-회원가입` | 기본 메시지를 그대로 둠. 태그·기능·변경 사항이 로그에 없음 |
| `Merge pull request #4` | 무엇을 머지했는지 알 수 없음 |

- 저장소 설정에서 머지 커밋 입력창이 **PR 제목 + 본문으로 미리 채워지게** 해 둔다 ([7절](#7-github-저장소-설정-체크리스트)). 작성자는 본문에서 "변경 사항" 목록과 `Closed #` 줄만 남기고 나머지를 지운다.
- 머지는 **approve 2개가 모인 뒤 PR 작성자가** 누른다. 메시지를 고쳐 쓰는 책임도 작성자에게 있다.
- 머지 후 브랜치는 삭제한다.

---

## 6. Code Review

- **매일 하루에 한 번** 열린 PR을 확인한다. 짧게 모여서(10분) 같이 보는 걸 권장.
- **approve 2개 이상**이어야 머지할 수 있다. (main 브랜치 보호 규칙으로 강제)
  - 3명 팀이므로 **작성자를 제외한 두 명이 모두 approve** 해야 한다.
  - 본인 PR은 본인이 approve할 수 없다.
- 리뷰 코멘트는 "왜"를 함께 쓴다. 단순 취향 차이는 `nit:` 접두어로 표시해 필수 아님을 밝힌다.
- 리뷰어가 볼 것:
  - [ ] PR 제목이 `태그: [기능번호] 제목 #이슈번호` 형식이고, 이슈와 기능 번호가 맞나
  - [ ] 브랜치 이름이 `종류/이슈번호-기능번호-영문이름` 형식인가
  - [ ] 종류 라벨 1개와 파트 라벨이 실제로 바뀐 패키지와 맞나 ([3절](#3-label-rule))
  - [ ] 이슈의 Tasks가 모두 체크됐나
  - [ ] 커밋마다 `태그: 제목` + body + `Refs #이슈번호`가 있나
  - [ ] `db.properties`, 비밀번호가 커밋에 섞이지 않았나
  - [ ] 다른 기능의 파일이 섞이지 않았나 (`git diff --stat`으로 확인)
  - [ ] GitHub에 "This branch has conflicts" 또는 "out-of-date"가 없나. 있으면 작성자에게 2절 순서대로 main pull을 요청한다
  - [ ] PR `참고 사항`의 확인 방법대로 돌려 봤을 때 실제로 동작하나

---

## 7. GitHub 저장소 설정 체크리스트

0단계 [환경] 이슈에서 처리한다.

- [ ] 라벨 생성 — [3절](#3-label-rule) "라벨 만들기"의 명령으로 종류 6개 + 파트 11개 (`수정(fix)`는 이미 있음)
- [ ] `.github/ISSUE_TEMPLATE/` — 현재 `FEATURE_TAMPLATE.md` 1개(라벨 `enhancement`)가 있음 → 파일명 오타 수정, 라벨을 `✨ Feature`로, 제목 기본값을 `[L-00]`으로, Describe에 기능 번호·예상 시간 칸 추가, bug / refactor / environment / suggestion 추가
- [x] `.github/PULL_REQUEST_TEMPLATE.md` — 이미 있음. 이 파일이 정본이므로 건드리지 않는다
- [ ] `main` 브랜치 보호: PR 필수, approve 2 필수, 직접 push 금지, 머지 전 최신 main 반영 필수
- [ ] Settings → Pull Requests: Merge commit만 허용, **머지 커밋 기본 메시지 = "Pull request title and description"**, 머지 후 브랜치 자동 삭제
- [ ] 이름이 규칙과 다른 원격 브랜치 정리 (`20-feat-f-04-AddMember-2`, `feature/#11-…` 등 머지됐거나 중복인 것)
- [ ] (선택) Projects 보드 — Todo / In Progress / In Review / Done

---

## 8. 전체 예시 — L-05 출결 입력을 처음부터 끝까지

담당 B가 [PLAN.md 4절](PLAN.md#4-기능)의 **L-05 출결 입력 (수동)** 을 만든다고 하자.

### ① 이슈 만들기 → GitHub가 #24를 붙인다

```bash
gh issue create \
  --title "[L-05] 출결 입력 — 입실·퇴실 시각으로 상태 판정" \
  --label "✨ Feature" --label "rule" --label "controller" \
  --body-file issue.md
# → https://github.com/…/issues/24
```

### ② 브랜치 만들기

```bash
git switch main
git pull origin main
git switch -c feat/24-L05-attendance-checkin
```

### ③ 커밋 — 작게 여러 번, 마지막 줄에 `Refs #24`

```bash
git add src/main/java/com/latecatcher/app/rule/AttendanceRule.java src/test/java/com/latecatcher/app/rule/AttendanceRuleTest.java
git commit
```

```text
feat: 입실 시각으로 지각 여부 판정

- course_tbl의 start_time보다 늦게 입실하면 LATE, 아니면 PRESENT로 판정한다.
- 09:00:00 정각 입실이 PRESENT인지 경계값 테스트를 추가했다.

Refs #24
```

```text
feat: 출결 입력 메뉴와 중복 날짜 안내 추가

- 출결 메뉴 2번에서 날짜·입실·퇴실을 입력받아 저장한다.
- 같은 날짜에 기록이 있으면 UNIQUE 위반을 잡아 "이미 기록이 있습니다"를 출력한다.

Refs #24
```

### ④ push 전에 최신 main을 pull해서 충돌 확인

```bash
git pull origin main
git status                 # "both modified"가 없어야 한다. 있으면 2절 "충돌이 났을 때"대로 해결
./gradlew compileJava      # 빌드 확인
```

### ⑤ push하고 PR 만들기 → GitHub가 #27을 붙인다

```bash
git push -u origin feat/24-L05-attendance-checkin
gh pr create \
  --title "feat: [L-05] 출결 입력과 상태 판정 #24" \
  --label "✨ Feature" --label "rule" --label "controller"
# 본문은 PR 템플릿 4개 항목을 채운다. 관련 이슈번호에 "Closed #24"
```

### ⑥ 리뷰 → approve 2개 → 작성자가 머지

머지 메시지 입력창을 다음처럼 고친다.

```text
feat: [L-05] 출결 입력과 상태 판정 #24

- 입실·퇴실 시각과 과정의 시작·종료 시각으로 PRESENT / LATE / EARLY_LEAVE를 판정한다.
- 같은 날짜 중복 입력은 저장하지 않고 안내한다.

Closed #24
```

### ⑦ 결과 — `main`의 로그만 보고도 기능과 이슈를 찾을 수 있다

```text
$ git log --oneline --graph -4
*   a1b2c3d feat: [L-05] 출결 입력과 상태 판정 #24
|\
| * 9f8e7d6 feat: 출결 입력 메뉴와 중복 날짜 안내 추가
| * 5a4b3c2 feat: 입실 시각으로 지각 여부 판정
|/
* 1234abc feat: [L-02] 로그인 성공 시 true 반환 #31
```

- 이슈 #24는 머지와 함께 자동으로 닫힌다.
- 이슈 #24의 타임라인에는 `Refs #24`를 단 커밋 두 개와 PR #27이 연결되어 보인다.
- 나중에 L-05에 버그가 나오면 `[L-05] …` 제목으로 새 이슈를 만들고 `fix/번호-L05-…` 브랜치로 고친다.
