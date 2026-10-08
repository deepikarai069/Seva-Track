package com.sevatrack.model;

import com.sevatrack.util.Fmt;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Complaint {
    private Long id;
    private String ticketNo;
    private String citizenName;
    private String citizenPhone;
    private String citizenEmail;
    private String title;
    private String description;
    private int categoryId;
    private String categoryName;
    private int wardId;
    private String wardName;
    private int departmentId;
    private String departmentName;
    private Priority priority;
    private Status status;
    private Integer assignedOfficerId;
    private String assignedOfficerName;
    private int escalationLevel;
    private LocalDateTime slaDueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private boolean slaBreached;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }
    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }
    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Integer getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String assignedOfficerName) { this.assignedOfficerName = assignedOfficerName; }
    public int getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }
    public LocalDateTime getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(LocalDateTime slaDueAt) { this.slaDueAt = slaDueAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public boolean isSlaBreached() { return slaBreached; }
    public void setSlaBreached(boolean slaBreached) { this.slaBreached = slaBreached; }

    public boolean isOpen() { return status != null && status.isOpen(); }
    public boolean isOverdue(LocalDateTime now) { return isOpen() && slaDueAt != null && slaDueAt.isBefore(now); }
    public boolean isPastDue() { return isOverdue(LocalDateTime.now()); }
    public String getCreatedAtText() { return Fmt.dateTime(createdAt); }
    public String getSlaDueAtText() { return Fmt.dateTime(slaDueAt); }
    public String getResolvedAtText() { return Fmt.dateTime(resolvedAt); }
    public String getLevelLabel() { return Fmt.levelLabel(escalationLevel); }
    /** Statuses an officer may move this complaint to next (used by the officer desk form). */
    public List<Status> getNextStatuses() {
        return Arrays.stream(Status.values()).filter(t -> t != status && status != null && status.canTransitionTo(t)).toList();
    }
}
