USE sevatrack;
DELIMITER $$

-- ---------------------------------------------------------------- functions
CREATE FUNCTION fn_open_load(p_officer INT) RETURNS INT
READS SQL DATA
BEGIN
  RETURN (SELECT COUNT(*) FROM complaints
           WHERE assigned_officer_id = p_officer
             AND status IN ('NEW','ASSIGNED','IN_PROGRESS'));
END$$

-- ---------------------------------------------------------------- triggers
CREATE TRIGGER trg_complaints_bu BEFORE UPDATE ON complaints
FOR EACH ROW
BEGIN
  IF NEW.status <> OLD.status THEN
    IF NOT ( (OLD.status = 'NEW'         AND NEW.status = 'ASSIGNED')
          OR (OLD.status = 'ASSIGNED'    AND NEW.status IN ('IN_PROGRESS','RESOLVED'))
          OR (OLD.status = 'IN_PROGRESS' AND NEW.status = 'RESOLVED')
          OR (OLD.status = 'RESOLVED'    AND NEW.status IN ('CLOSED','IN_PROGRESS')) ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Illegal complaint status transition';
    END IF;
    IF NEW.status = 'RESOLVED' THEN SET NEW.resolved_at = NOW(); END IF;
    IF OLD.status = 'RESOLVED' AND NEW.status = 'IN_PROGRESS' THEN SET NEW.resolved_at = NULL; END IF;
  END IF;
  IF NEW.escalation_level < OLD.escalation_level THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Escalation level cannot decrease';
  END IF;
END$$

CREATE TRIGGER trg_complaints_ai AFTER INSERT ON complaints
FOR EACH ROW
BEGIN
  INSERT INTO status_history(complaint_id, old_status, new_status, old_officer_id, new_officer_id,
                             old_level, new_level, actor, note)
  VALUES (NEW.id, NULL, NEW.status, NULL, NEW.assigned_officer_id, NULL, NEW.escalation_level,
          'CITIZEN', 'Complaint registered');
END$$

CREATE TRIGGER trg_complaints_au AFTER UPDATE ON complaints
FOR EACH ROW
BEGIN
  IF NOT (NEW.status <=> OLD.status)
     OR NOT (NEW.assigned_officer_id <=> OLD.assigned_officer_id)
     OR NEW.escalation_level <> OLD.escalation_level
     OR NEW.sla_breached <> OLD.sla_breached THEN
    INSERT INTO status_history(complaint_id, old_status, new_status, old_officer_id, new_officer_id,
                               old_level, new_level, actor, note)
    VALUES (NEW.id, OLD.status, NEW.status, OLD.assigned_officer_id, NEW.assigned_officer_id,
            OLD.escalation_level, NEW.escalation_level, COALESCE(@st_actor, 'SYSTEM'), @st_note);
  END IF;
END$$

-- ---------------------------------------------------------------- procedures
CREATE PROCEDURE sp_assign_complaint(IN p_id BIGINT, IN p_officer INT, IN p_due DATETIME,
                                     IN p_actor VARCHAR(100), IN p_note VARCHAR(255))
BEGIN
  DECLARE v_status VARCHAR(20);
  SET v_status = (SELECT status FROM complaints WHERE id = p_id);
  IF v_status IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Complaint not found';
  END IF;
  IF v_status NOT IN ('NEW','ASSIGNED','IN_PROGRESS') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resolved or closed complaints cannot be reassigned';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM officers WHERE id = p_officer AND active = 1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Officer not found or inactive';
  END IF;
  SET @st_actor = p_actor, @st_note = p_note;
  UPDATE complaints
     SET assigned_officer_id = p_officer,
         status     = IF(status = 'NEW', 'ASSIGNED', status),
         sla_due_at = COALESCE(p_due, sla_due_at)
   WHERE id = p_id;
END$$

CREATE PROCEDURE sp_update_status(IN p_id BIGINT, IN p_status VARCHAR(20),
                                  IN p_actor VARCHAR(100), IN p_note VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM complaints WHERE id = p_id) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Complaint not found';
  END IF;
  SET @st_actor = p_actor, @st_note = p_note;
  UPDATE complaints SET status = p_status WHERE id = p_id;
END$$

CREATE PROCEDURE sp_escalate_complaint(IN p_id BIGINT, IN p_officer INT, IN p_level TINYINT,
                                       IN p_due DATETIME, IN p_reason VARCHAR(255))
BEGIN
  DECLARE v_status VARCHAR(20);
  DECLARE v_level  TINYINT;
  DECLARE v_from   INT;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  SET v_status = (SELECT status FROM complaints WHERE id = p_id);
  SET v_level  = (SELECT escalation_level FROM complaints WHERE id = p_id);
  SET v_from   = (SELECT assigned_officer_id FROM complaints WHERE id = p_id);
  IF v_status IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Complaint not found';
  END IF;
  IF v_status NOT IN ('NEW','ASSIGNED','IN_PROGRESS') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only open complaints can be escalated';
  END IF;
  IF p_level <= v_level THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Escalation must move to a higher level';
  END IF;

  START TRANSACTION;
  SET @st_actor = 'SLA-MONITOR', @st_note = p_reason;
  UPDATE complaints
     SET assigned_officer_id = p_officer,
         escalation_level    = p_level,
         status     = IF(status = 'NEW', 'ASSIGNED', status),
         sla_due_at = p_due
   WHERE id = p_id;
  INSERT INTO escalations(complaint_id, from_level, to_level, from_officer_id, to_officer_id, reason)
  VALUES (p_id, v_level, p_level, v_from, p_officer, p_reason);
  COMMIT;
END$$

CREATE PROCEDURE sp_mark_breached(IN p_id BIGINT, IN p_note VARCHAR(255))
BEGIN
  SET @st_actor = 'SLA-MONITOR', @st_note = p_note;
  UPDATE complaints SET sla_breached = 1 WHERE id = p_id AND sla_breached = 0;
END$$

CREATE PROCEDURE sp_department_report(IN p_from DATETIME, IN p_to DATETIME)
BEGIN
  SELECT d.id AS department_id, d.name AS department_name,
         COUNT(c.id)                                                          AS total,
         COALESCE(SUM(c.status IN ('NEW','ASSIGNED','IN_PROGRESS')), 0)       AS open_count,
         COALESCE(SUM(c.status IN ('RESOLVED','CLOSED')), 0)                  AS resolved_count,
         COALESCE(SUM(c.escalation_level > 1), 0)                             AS escalated_count,
         COALESCE(SUM(c.sla_breached), 0)                                     AS breached_count,
         COALESCE(ROUND(AVG(TIMESTAMPDIFF(MINUTE, c.created_at, c.resolved_at)) / 60, 2), 0) AS avg_hours
    FROM departments d
    LEFT JOIN complaints c ON c.department_id = d.id AND c.created_at >= p_from AND c.created_at < p_to
   GROUP BY d.id, d.name
   ORDER BY d.id;
END$$

DELIMITER ;
