package com.sevatrack.dao;

import com.sevatrack.model.Complaint;
import com.sevatrack.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComplaintDao {
    long insert(Complaint c);

    Optional<Complaint> findById(long id);

    Optional<Complaint> findByTicket(String ticketNo);

    List<Complaint> findByOfficer(int officerId);

    /** Open complaints whose SLA deadline has passed and which still need escalation work. */
    List<Complaint> findOverdue(LocalDateTime now);

    List<Complaint> findRecent(int limit);

    void assign(long id, int officerId, LocalDateTime newDue, String actor, String note);

    void updateStatus(long id, Status status, String actor, String note);

    void escalate(long id, int toOfficerId, int toLevel, LocalDateTime newDue, String reason);

    void markBreached(long id, String note);
}
