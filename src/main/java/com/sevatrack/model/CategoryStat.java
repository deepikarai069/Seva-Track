package com.sevatrack.model;

public class CategoryStat {
    private int departmentId;
    private String categoryName;
    private int total;
    private int open;

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getOpen() { return open; }
    public void setOpen(int open) { this.open = open; }
}
