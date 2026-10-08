package com.sevatrack.service;

public class ComplaintForm {
    private String citizenName;
    private String citizenPhone;
    private String citizenEmail;
    private String title;
    private String description;
    private int categoryId;
    private int wardId;
    private String priority;

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
    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
