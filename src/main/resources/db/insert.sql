USE smsdb;

START TRANSACTION;

SET @now = NOW();


-- 회원 더미 데이터
INSERT INTO member_tbl (
    name,
    age,
    phone
)
VALUES
    ('김철수', 25, '010-1111-1111'),
    ('이영희', 30, '010-2222-2222'),
    ('박지성', 43, '010-3333-3333'),
    ('손흥민', 32, '010-4444-4444'),
    ('김연아', 34, '010-5555-5555'),
    ('류현진', 37, '010-6666-6666'),
    ('박세리', 47, '010-7777-7777'),
    ('이정재', 52, '010-8888-8888'),
    ('송중기', 39, '010-9999-9999'),
    ('전지현', 43, '010-0000-0000');


-- 김철수: 넷플릭스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-1111-1111'),
           'NETFLIX',
           'PREMIUM',
           @now,
           DATE_ADD(@now, INTERVAL 1 MONTH),
           TRUE
       );


-- 김철수: 티빙
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-1111-1111'),
           'TVING',
           'STANDARD',
           DATE_SUB(@now, INTERVAL 10 DAY),
           DATE_ADD(@now, INTERVAL 20 DAY),
           TRUE
       );


-- 이영희: 디즈니 플러스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-2222-2222'),
           'DISNEY_PLUS',
           'PREMIUM',
           @now,
           DATE_ADD(@now, INTERVAL 1 YEAR),
           TRUE
       );


-- 박지성: 웨이브
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-3333-3333'),
           'WAVVE',
           'BASIC',
           DATE_SUB(@now, INTERVAL 27 DAY),
           DATE_ADD(@now, INTERVAL 3 DAY),
           TRUE
       );


-- 박지성: 쿠팡플레이
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-3333-3333'),
           'COUPANG_PLAY',
           'WOW',
           DATE_SUB(@now, INTERVAL 3 MONTH),
           DATE_ADD(@now, INTERVAL 9 MONTH),
           TRUE
       );


-- 손흥민: 만료된 넷플릭스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-4444-4444'),
           'NETFLIX',
           'STANDARD',
           DATE_SUB(@now, INTERVAL 2 MONTH),
           DATE_SUB(@now, INTERVAL 1 MONTH),
           FALSE
       );


-- 김연아: 애플TV 플러스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-5555-5555'),
           'APPLE_TV_PLUS',
           'STANDARD',
           @now,
           DATE_ADD(@now, INTERVAL 1 YEAR),
           TRUE
       );


-- 류현진: 넷플릭스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-6666-6666'),
           'NETFLIX',
           'BASIC',
           DATE_SUB(@now, INTERVAL 27 DAY),
           DATE_ADD(@now, INTERVAL 3 DAY),
           TRUE
       );


-- 박세리: 디즈니 플러스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-7777-7777'),
           'DISNEY_PLUS',
           'STANDARD',
           @now,
           DATE_ADD(@now, INTERVAL 1 YEAR),
           TRUE
       );


-- 박세리: 웨이브
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-7777-7777'),
           'WAVVE',
           'PREMIUM',
           @now,
           DATE_ADD(@now, INTERVAL 1 MONTH),
           TRUE
       );


-- 이정재: 만료된 티빙
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-8888-8888'),
           'TVING',
           'BASIC',
           DATE_SUB(@now, INTERVAL 1 MONTH),
           DATE_SUB(@now, INTERVAL 1 DAY),
           FALSE
       );


-- 송중기: 넷플릭스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-9999-9999'),
           'NETFLIX',
           'PREMIUM',
           @now,
           DATE_ADD(@now, INTERVAL 1 MONTH),
           TRUE
       );


-- 송중기: 애플TV 플러스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-9999-9999'),
           'APPLE_TV_PLUS',
           'STANDARD',
           @now,
           DATE_ADD(@now, INTERVAL 1 MONTH),
           TRUE
       );


-- 전지현: 디즈니 플러스
INSERT INTO membership_tbl (
    member_id,
    ott_type,
    plan_name,
    enroll_date,
    expiry_date,
    active
)
VALUES (
           (SELECT member_id
            FROM member_tbl
            WHERE phone = '010-0000-0000'),
           'DISNEY_PLUS',
           'PREMIUM',
           @now,
           DATE_ADD(@now, INTERVAL 1 YEAR),
           TRUE
       );

COMMIT;
