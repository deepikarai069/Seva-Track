package com.sevatrack.model;

import com.sevatrack.util.Fmt;
import java.time.LocalDateTime;

public class StatusHistory {
    private long id;
    private long complaintId;
    private Status oldStatus;
    private Status newStatus;
    private String oldOfficerName;
    private String newOfficerName;
    private Integer oldLevel;
    private int newLevel;
    private String actor;
    private String note;
    private LocalDateTime changedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getComplaintId() { return complaintId; }
    public void setComplaintId(long complaintId) { this.complaintId = complaintId; }
    public Status getOldStatus() { return oldStatus; }
    public void setOldStatus(Status oldStatus) { this.oldStatus = oldStatus; }
    public Status getNewStatus() { return newStatus; }
    public void setNewStatus(Status newStatus) { this.newStatus = newStatus; }
    public String getOldOfficerName() { return oldOfficerName; }
    public void setOldOfficerName(String oldOfficerName) { this.oldOfficerName = oldOfficerName; }
    public String getNewOfficerName() { return newOfficerName; }
    public void setNewOfficerName(String newOfficerName) { this.newOfficerName = newOfficerName; }
    public Integer getOldLevel() { return oldLevel; }
    public void setOldLevel(Integer oldLevel) { this.oldLevel = oldLevel; }
    public int getNewLevel() { return newLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getChangedAtText() { return Fmt.dateTime(changedAt); }
    public String getLevelText() { return oldLevel != null && oldLevel != newLevel ? "L" + oldLevel + " \u2192 L" + newLevel : "L" + newLevel; }
}
