-- SevaTrack schema (MariaDB 10.5+ / MySQL 8)
CREATE DATABASE IF NOT EXISTS sevatrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sevatrack;

CREATE TABLE departments (
  id   INT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  code VARCHAR(8)  NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE wards (
  id   INT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE categories (
  id               INT PRIMARY KEY,
  name             VARCHAR(80) NOT NULL,
  department_id    INT NOT NULL,
  default_priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
  CONSTRAINT fk_cat_dept FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- level 1 = ward officer, 2 = department supervisor, 3 = department head
CREATE TABLE officers (
  id            INT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  department_id INT NOT NULL,
  ward_id       INT NULL,                       -- NULL = floating / department-wide
  level         TINYINT NOT NULL,
  max_load      INT NOT NULL DEFAULT 10,
  emergency     TINYINT(1) NOT NULL DEFAULT 0,  -- certified for HIGH / CRITICAL complaints
  active        TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT chk_level CHECK (level BETWEEN 1 AND 3),
  CONSTRAINT fk_off_dept FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT fk_off_ward FOREIGN KEY (ward_id) REFERENCES wards(id),
  INDEX idx_off_route (department_id, level, active)
) ENGINE=InnoDB;

-- SLA hours allowed at each escalation level, per priority
CREATE TABLE sla_policy (
  priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  level    TINYINT NOT NULL,
  hours    INT NOT NULL,
  PRIMARY KEY (priority, level)
) ENGINE=InnoDB;

CREATE TABLE complaints (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticket_no           VARCHAR(24) NOT NULL UNIQUE,
  citizen_name        VARCHAR(100) NOT NULL,
  citizen_phone       VARCHAR(20)  NOT NULL,
  citizen_email       VARCHAR(120) NULL,
  title               VARCHAR(150) NOT NULL,
  description         TEXT NOT NULL,
  category_id         INT NOT NULL,
  ward_id             INT NOT NULL,
  department_id       INT NOT NULL,
  priority            ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  status              ENUM('NEW','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'NEW',
  assigned_officer_id INT NULL,
  escalation_level    TINYINT NOT NULL DEFAULT 1,
  sla_due_at          DATETIME NOT NULL,
  sla_breached        TINYINT(1) NOT NULL DEFAULT 0,   -- 1 = missed SLA even at the final level
  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  resolved_at         DATETIME NULL,
  CONSTRAINT fk_c_cat  FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_c_ward FOREIGN KEY (ward_id) REFERENCES wards(id),
  CONSTRAINT fk_c_dept FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT fk_c_off  FOREIGN KEY (assigned_officer_id) REFERENCES officers(id),
  INDEX idx_c_sla (status, sla_due_at),
  INDEX idx_c_officer (assigned_officer_id, status),
  INDEX idx_c_dept_created (department_id, created_at)
) ENGINE=InnoDB;

-- Audit trail, written ONLY by triggers on complaints
CREATE TABLE status_history (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id   BIGINT NOT NULL,
  old_status     ENUM('NEW','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NULL,
  new_status     ENUM('NEW','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL,
  old_officer_id INT NULL,
  new_officer_id INT NULL,
  old_level      TINYINT NULL,
  new_level      TINYINT NOT NULL,
  actor          VARCHAR(100) NOT NULL,
  note           VARCHAR(255) NULL,
  changed_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sh_c FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  INDEX idx_sh_c (complaint_id, changed_at)
) ENGINE=InnoDB;

CREATE TABLE escalations (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id     BIGINT NOT NULL,
  from_level       TINYINT NOT NULL,
  to_level         TINYINT NOT NULL,
  from_officer_id  INT NULL,
  to_officer_id    INT NOT NULL,
  reason           VARCHAR(255) NOT NULL,
  escalated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_e_c FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  INDEX idx_e_c (complaint_id)
) ENGINE=InnoDB;
