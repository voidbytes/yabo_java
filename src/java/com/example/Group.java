package com.example;

public class Group {
    private Integer id;
    private String groupName;
    private Integer isDefault;
    private Integer userId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() {
        return groupName;
    }
}