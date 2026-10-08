package com.sevatrack.model;

import com.sevatrack.util.Fmt;
import java.time.LocalDateTime;

public class EscalationRecord {
    private long id;
    private long complaintId;
    private int fromLevel;
    private int toLevel;
    private String fromOfficerName;
    private String toOfficerName;
    private String reason;
    private LocalDateTime escalatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getComplaintId() { return complaintId; }
    public void setComplaintId(long complaintId) { this.complaintId = complaintId; }
    public int getFromLevel() { return fromLevel; }
    public void setFromLevel(int fromLevel) { this.fromLevel = fromLevel; }
    public int getToLevel() { return toLevel; }
    public void setToLevel(int toLevel) { this.toLevel = toLevel; }
    public String getFromOfficerName() { return fromOfficerName; }
    public void setFromOfficerName(String fromOfficerName) { this.fromOfficerName = fromOfficerName; }
    public String getToOfficerName() { return toOfficerName; }
    public void setToOfficerName(String toOfficerName) { this.toOfficerName = toOfficerName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getEscalatedAtText() { return Fmt.dateTime(escalatedAt); }
}
