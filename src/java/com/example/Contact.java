package com.example;

public class Contact {
    private Integer id;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String county;
    private String company;
    private String email;
    private String remark;
    private Integer groupId;
    private Integer userId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    private String groupName;

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}