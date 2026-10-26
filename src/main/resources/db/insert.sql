-- 지각 잡는 자바 샘플 데이터. schema.sql을 먼저 실행한다
-- 출결은 2026-10-21(작성일 전날)까지만 넣는다. 계산 기준일이 어제이기 때문이다 (PLAN.md 3절)
USE latedb;

START TRANSACTION;


-- 과정 1행: 시각·기준은 DEFAULT(09:20~18:20, 3회 = 결석 1일, 최소 수업 시간 240분, 80%)를 쓴다
INSERT INTO course_tbl (
    name,
    start_date,
    end_date
)
VALUES ('[솔트룩스]LLM 지능형 에이전트 서비스개발 부트캠프', '2026-09-29', '2027-03-24');


-- 회원: 팀 3명 + 미달 사례용 더미 1명
INSERT INTO member_tbl (
    name,
    age,
    phone
)
VALUES
    ('RY', 26, '010-1111-1111'),
    ('YY', 31, '010-2222-2222'),
    ('YI', 28, '010-3333-3333'),
    ('홍길동', 35, '010-4444-4444');


-- 수업일 캘린더: 훈련 기간의 평일을 모두 넣는다 (L-04와 같은 결과)
SELECT start_date, end_date INTO @start, @end FROM course_tbl;

INSERT INTO class_day_tbl (class_date)
WITH RECURSIVE days AS (
    SELECT @start AS d
    UNION ALL
    SELECT d + INTERVAL 1 DAY FROM days WHERE d < @end
)
SELECT d
FROM days
WHERE DAYOFWEEK(d) NOT IN (1, 7);   -- 1 = 일요일, 7 = 토요일


-- 평일 공휴일은 수업일에서 뺀다. 수업일 120일 (고용24 총 훈련일수와 같다)
UPDATE class_day_tbl SET is_class_day = FALSE, note = '대체공휴일(개천절)' WHERE class_date = '2026-10-05';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '한글날'            WHERE class_date = '2026-10-09';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '성탄절'            WHERE class_date = '2026-12-25';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '신정'              WHERE class_date = '2027-01-01';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '설날'              WHERE class_date = '2027-02-08';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '대체공휴일(설날)'  WHERE class_date = '2027-02-09';
UPDATE class_day_tbl SET is_class_day = FALSE, note = '삼일절'            WHERE class_date = '2027-03-01';


SET @ry   = (SELECT member_id FROM member_tbl WHERE phone = '010-1111-1111');
SET @yy   = (SELECT member_id FROM member_tbl WHERE phone = '010-2222-2222');
SET @yi   = (SELECT member_id FROM member_tbl WHERE phone = '010-3333-3333');
SET @hong = (SELECT member_id FROM member_tbl WHERE phone = '010-4444-4444');


-- RY: 모범 사례. 10월 기록 13일, 공결 1 → 출석률 100%
INSERT INTO attendance_tbl (
    member_id,
    attend_date,
    check_in,
    check_out,
    status,
    memo
)
VALUES
    (@ry, '2026-09-29', '09:08', '18:25', 'PRESENT', NULL),
    (@ry, '2026-09-30', '09:11', '18:22', 'PRESENT', NULL),
    (@ry, '2026-10-01', '09:07', '18:30', 'PRESENT', NULL),
    (@ry, '2026-10-02', '09:15', '18:21', 'PRESENT', NULL),
    (@ry, '2026-10-06', '09:10', '18:23', 'PRESENT', NULL),
    (@ry, '2026-10-07', '09:12', '18:27', 'PRESENT', NULL),
    (@ry, '2026-10-08', '09:09', '18:24', 'PRESENT', NULL),
    (@ry, '2026-10-12', '09:13', '18:22', 'PRESENT', NULL),
    (@ry, '2026-10-13', '09:06', '18:26', 'PRESENT', NULL),
    (@ry, '2026-10-14', NULL,    NULL,    'EXCUSED', '병원 진료 (진단서 제출)'),
    (@ry, '2026-10-15', '09:14', '18:21', 'PRESENT', NULL),
    (@ry, '2026-10-16', '09:10', '18:25', 'PRESENT', NULL),
    (@ry, '2026-10-19', '09:11', '18:23', 'PRESENT', NULL),
    (@ry, '2026-10-20', '09:09', '18:28', 'PRESENT', NULL),
    (@ry, '2026-10-21', '09:12', '18:22', 'PRESENT', NULL);


-- YY: 경계 사례. 10월 기록 12일 (10/16 미입력)
--     결석 1 + floor((지각 3 + 조퇴 1 + 외출 1) / 3) = 환산 결석 2 → (12 - 2) / 12 = 83.3%
--     지각 1번 더면 환산 결석 3 → 80% 아래
INSERT INTO attendance_tbl (
    member_id,
    attend_date,
    check_in,
    check_out,
    status,
    memo
)
VALUES
    (@yy, '2026-09-29', '09:15', '18:22', 'PRESENT',     NULL),
    (@yy, '2026-09-30', '09:17', '18:21', 'PRESENT',     NULL),
    (@yy, '2026-10-01', '09:18', '18:23', 'PRESENT',     NULL),
    (@yy, '2026-10-02', '09:27', '18:22', 'LATE',        NULL),
    (@yy, '2026-10-06', '09:16', '18:21', 'PRESENT',     NULL),
    (@yy, '2026-10-07', '09:35', '18:24', 'LATE',        '버스 지연'),
    (@yy, '2026-10-08', '09:19', '18:22', 'PRESENT',     NULL),
    (@yy, '2026-10-12', NULL,    NULL,    'ABSENT',      NULL),
    (@yy, '2026-10-13', '09:14', '18:23', 'PRESENT',     NULL),
    (@yy, '2026-10-14', '09:17', '18:21', 'OUTING',      '외출 14:00~15:30 은행'),
    (@yy, '2026-10-15', '09:18', '17:30', 'EARLY_LEAVE', NULL),
    (@yy, '2026-10-19', '09:52', '18:22', 'LATE',        NULL),
    (@yy, '2026-10-20', '09:15', '18:25', 'PRESENT',     NULL),
    (@yy, '2026-10-21', '09:16', '18:21', 'PRESENT',     NULL);


-- YI: 기록 없음 사례. 9월만 있고 10월 기록이 없다 (erd.md 7절의 예)
INSERT INTO attendance_tbl (
    member_id,
    attend_date,
    check_in,
    check_out,
    status,
    memo
)
VALUES
    (@yi, '2026-09-29', '09:13', '18:24', 'PRESENT', NULL),
    (@yi, '2026-09-30', '09:10', '18:22', 'PRESENT', NULL);


-- 홍길동: 미달 사례. 10월 기록 13일, 공결 1
--     10/13 지각·조퇴: 09:40 입실, 16:50 퇴실 → 수업 7시간 10분(4시간 이상)이라 지각 1 + 조퇴 1로 인정
--     10/15 결석: 10:20 입실, 13:20 퇴실 → 수업 3시간(4시간 미만)이라 결석
--     결석 2 + floor((지각 4 + 조퇴 1) / 3) = 환산 결석 3 → (13 - 1 - 3) / (13 - 1) = 75.0%
--     (뷰 기준: late_cnt 4, early_cnt 1. 10/13은 두 개수에 모두 포함된다. record_cnt는 13)
INSERT INTO attendance_tbl (
    member_id,
    attend_date,
    check_in,
    check_out,
    status,
    memo
)
VALUES
    (@hong, '2026-09-29', '09:18', '18:21', 'PRESENT',     NULL),
    (@hong, '2026-09-30', '09:41', '18:23', 'LATE',        NULL),
    (@hong, '2026-10-01', '09:17', '18:22', 'PRESENT',     NULL),
    (@hong, '2026-10-02', NULL,    NULL,    'ABSENT',      NULL),
    (@hong, '2026-10-06', '09:32', '18:21', 'LATE',        NULL),
    (@hong, '2026-10-07', '09:19', '18:24', 'PRESENT',     NULL),
    (@hong, '2026-10-08', NULL,    NULL,    'EXCUSED',     '예비군 훈련'),
    (@hong, '2026-10-12', '09:15', '18:22', 'PRESENT',     NULL),
    (@hong, '2026-10-13', '09:40', '16:50', 'LATE_EARLY_LEAVE', NULL),
    (@hong, '2026-10-14', '09:16', '18:21', 'PRESENT',     NULL),
    (@hong, '2026-10-15', '10:20', '13:20', 'ABSENT',      '수업 3시간(최소 4시간 미만)'),
    (@hong, '2026-10-16', '10:00', '18:23', 'LATE',        NULL),
    (@hong, '2026-10-19', '09:17', '18:22', 'PRESENT',     NULL),
    (@hong, '2026-10-20', '09:25', '18:21', 'LATE',        NULL),
    (@hong, '2026-10-21', '09:19', '18:25', 'PRESENT',     NULL);

COMMIT;
