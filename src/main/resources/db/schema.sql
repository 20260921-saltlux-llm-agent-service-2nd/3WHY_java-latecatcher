CREATE DATABASE IF NOT EXISTS smsdb;

# CREATE USER IF NOT EXISTS 'bear'@'%' IDENTIFIED BY 'bear';
# GRANT ALL PRIVILEGES ON smsdb.* TO 'bear'@'%';

USE smsdb;

DROP TABLE IF EXISTS membership_tbl;
DROP TABLE IF EXISTS member_tbl;

CREATE TABLE member_tbl
(
    member_id INT AUTO_INCREMENT,
    name      VARCHAR(30) NOT NULL,
    age       INT         NOT NULL,
    phone     VARCHAR(20) NOT NULL,
    CONSTRAINT pk_member_tbl PRIMARY KEY (member_id),
    CONSTRAINT uq_member_tbl_phone UNIQUE (phone)
);

CREATE TABLE membership_tbl
(
    membership_id INT AUTO_INCREMENT,
    member_id     INT          NOT NULL,
    ott_type      VARCHAR(30)  NOT NULL,
    plan_name     VARCHAR(50)  NOT NULL,
    enroll_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date   DATETIME     NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_membership_tbl PRIMARY KEY (membership_id),
    CONSTRAINT fk_membership_tbl_member_tbl
        FOREIGN KEY (member_id) REFERENCES member_tbl (member_id)
);
