package com.sevatrack.model;

public class DepartmentStat {
    private int departmentId;
    private String departmentName;
    private int total;
    private int open;
    private int resolved;
    private int escalated;
    private int breached;
    private double avgResolutionHours;

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getOpen() { return open; }
    public void setOpen(int open) { this.open = open; }
    public int getResolved() { return resolved; }
    public void setResolved(int resolved) { this.resolved = resolved; }
    public int getEscalated() { return escalated; }
    public void setEscalated(int escalated) { this.escalated = escalated; }
    public int getBreached() { return breached; }
    public void setBreached(int breached) { this.breached = breached; }
    public double getAvgResolutionHours() { return avgResolutionHours; }
    public void setAvgResolutionHours(double avgResolutionHours) { this.avgResolutionHours = avgResolutionHours; }
}
