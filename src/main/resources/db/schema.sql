-- 지각 잡는 자바 스키마. 기준: docs/erd.md (10/22 ERD 회의 합의본), DDL 원본은 docs/PLAN.md 7절
CREATE DATABASE IF NOT EXISTS latedb DEFAULT CHARACTER SET utf8mb4;

# CREATE USER IF NOT EXISTS 'bear'@'%' IDENTIFIED BY 'bear';
# GRANT ALL PRIVILEGES ON latedb.* TO 'bear'@'%';

USE latedb;

-- 자식부터 지운다 (FK 순서)
DROP VIEW IF EXISTS v_monthly_attendance;
DROP TABLE IF EXISTS import_log_tbl;
DROP TABLE IF EXISTS attendance_tbl;
DROP TABLE IF EXISTS class_day_tbl;
DROP TABLE IF EXISTS member_tbl;
DROP TABLE IF EXISTS course_tbl;

CREATE TABLE course_tbl                             -- 과정 설정, 1행만 사용. 다른 테이블과 잇지 않는다
(
    course_id            INT AUTO_INCREMENT,
    name                 VARCHAR(100) NOT NULL,
    start_date           DATE         NOT NULL,
    end_date             DATE         NOT NULL,
    start_time           TIME         NOT NULL DEFAULT '09:20:00',  -- slx-ai-campus 수업 시작
    end_time             TIME         NOT NULL DEFAULT '18:20:00',  -- slx-ai-campus 수업 종료
    partial_per_absence  INT          NOT NULL DEFAULT 3,   -- 지각·조퇴·외출 n회 = 결석 1일
    min_attend_minutes   INT          NOT NULL DEFAULT 240, -- 최소 수업 시간(분). 퇴실 − (입실과 시작 중 늦은 쪽)이 이보다 짧으면 결석
    allowance_threshold  INT          NOT NULL DEFAULT 80,  -- 장려금 기준 %
    completion_threshold INT          NOT NULL DEFAULT 80,  -- 수료 기준 %
    PRIMARY KEY (course_id),
    CONSTRAINT ck_course_tbl_period CHECK (end_date >= start_date)
);

CREATE TABLE member_tbl                             -- 스켈레톤 그대로(이름·나이·휴대폰 번호). 휴대폰 번호로 로그인
(
    member_id INT AUTO_INCREMENT,
    name      VARCHAR(30)              NOT NULL,                   -- 성과 이름을 띄어쓰기 없이 (예: 홍길동)
    age       INT                      NOT NULL,                   -- 1 이상은 Java가 확인한다 (스켈레톤)
    phone     VARCHAR(20)              NOT NULL,                   -- 로그인 키, 010-1234-5678 형식
    role      ENUM ('ADMIN','TRAINEE') NOT NULL DEFAULT 'TRAINEE', -- v0.2 권한(L-16)용. v0.1 코드는 쓰지 않는다
    PRIMARY KEY (member_id),
    CONSTRAINT uq_member_tbl_phone UNIQUE (phone),
    CONSTRAINT ck_member_tbl_phone CHECK (phone REGEXP '^010-[0-9]{4}-[0-9]{4}$')
);

CREATE TABLE class_day_tbl                          -- 수업일 캘린더
(
    class_date   DATE    NOT NULL,
    is_class_day BOOLEAN NOT NULL DEFAULT TRUE,     -- FALSE = 공휴일/휴강
    note         VARCHAR(255),                      -- '개천절', '휴강'
    PRIMARY KEY (class_date)
);

CREATE TABLE attendance_tbl                         -- 출결 1건 = 훈련생 1명의 하루
(
    attendance_id INT AUTO_INCREMENT,
    member_id     INT  NOT NULL,
    attend_date   DATE NOT NULL,
    check_in      TIME,
    check_out     TIME,
    status        ENUM ('PRESENT','LATE','EARLY_LEAVE','LATE_EARLY_LEAVE','OUTING','ABSENT','EXCUSED') NOT NULL, -- LATE_EARLY_LEAVE = 같은 날 지각과 조퇴를 모두 인정 (환산 2회)
    memo          VARCHAR(255),
    source        ENUM ('MANUAL','WORK24') NOT NULL DEFAULT 'MANUAL', -- v0.2 가져오기(L-06)용. v0.1은 항상 MANUAL
    PRIMARY KEY (attendance_id),
    CONSTRAINT fk_attendance_tbl_member_tbl
        FOREIGN KEY (member_id) REFERENCES member_tbl (member_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_tbl_class_day_tbl
        FOREIGN KEY (attend_date) REFERENCES class_day_tbl (class_date),
    CONSTRAINT uq_attendance_tbl_member_date UNIQUE (member_id, attend_date),
    CONSTRAINT ck_attendance_tbl_time CHECK (check_out IS NULL OR check_in IS NULL OR check_out >= check_in)
);

CREATE INDEX idx_attendance_tbl_date ON attendance_tbl (attend_date);

-- v0.2 가져오기(L-06)용. 확장을 고려해 v0.1 스키마에 미리 둔다. v0.1 코드는 쓰지 않는다
CREATE TABLE import_log_tbl                         -- 출결 가져오기 기록 (트랜잭션의 일부)
(
    import_id   INT AUTO_INCREMENT,
    file_name   VARCHAR(255) NOT NULL,
    row_count   INT          NOT NULL,
    imported_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    imported_by INT,
    PRIMARY KEY (import_id),
    CONSTRAINT fk_import_log_tbl_member_tbl
        FOREIGN KEY (imported_by) REFERENCES member_tbl (member_id) ON DELETE CASCADE -- 탈퇴(L-11)하면 가져오기 기록도 지운다
);

-- 월별 집계 뷰: SQL은 상태별 개수만 구하고, 출석률 계산은 Java(AttendanceRule)가 한다
-- LATE_EARLY_LEAVE(지각·조퇴)인 날은 late_cnt와 early_cnt에 모두 포함한다. 그래서 cnt 컬럼의 합은 기록 일수가 아니다
-- 기록 일수는 record_cnt를 쓴다
CREATE OR REPLACE VIEW v_monthly_attendance AS
SELECT m.member_id,
       m.name,
       DATE_FORMAT(a.attend_date, '%Y-%m')                  AS ym,
       COUNT(*)                                             AS record_cnt,
       SUM(a.status = 'PRESENT')                            AS present_cnt,
       SUM(a.status IN ('LATE', 'LATE_EARLY_LEAVE'))        AS late_cnt,
       SUM(a.status IN ('EARLY_LEAVE', 'LATE_EARLY_LEAVE')) AS early_cnt,
       SUM(a.status = 'OUTING')                             AS outing_cnt,
       SUM(a.status = 'ABSENT')                             AS absent_cnt,
       SUM(a.status = 'EXCUSED')                            AS excused_cnt
FROM member_tbl m
JOIN attendance_tbl a ON a.member_id = m.member_id
GROUP BY m.member_id, m.name, DATE_FORMAT(a.attend_date, '%Y-%m');
