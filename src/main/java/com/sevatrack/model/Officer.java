package com.sevatrack.model;

import com.sevatrack.util.Fmt;

public class Officer {
    private int id;
    private String name;
    private String email;
    private int departmentId;
    private String departmentName;
    private Integer wardId;
    private String wardName;
    private int level;
    private int maxLoad;
    private int openLoad;
    private boolean emergency;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getWardId() { return wardId; }
    public void setWardId(Integer wardId) { this.wardId = wardId; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getMaxLoad() { return maxLoad; }
    public void setMaxLoad(int maxLoad) { this.maxLoad = maxLoad; }
    public int getOpenLoad() { return openLoad; }
    public void setOpenLoad(int openLoad) { this.openLoad = openLoad; }
    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }

    public double getLoadRatio() { return maxLoad <= 0 ? 1.0 : (double) openLoad / maxLoad; }
    public String getLevelLabel() { return Fmt.levelLabel(level); }
}
