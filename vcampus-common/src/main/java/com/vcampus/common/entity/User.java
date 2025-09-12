package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * 支持学生、教师、管理员三种角色
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户角色枚举
    public enum Role {
        STUDENT("学生"),
        TEACHER("教师"),
        ADMIN("管理员");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 性别枚举
    public enum Gender {
        MALE("男"),
        FEMALE("女");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String userId;        // 用户ID（学号/工号）
    private String password;      // 密码
    private String realName;      // 真实姓名
    private Role role;           // 用户角色
    private Gender gender;       // 性别
    private int age;             // 年龄
    private String email;        // 邮箱
    private String phone;        // 电话
    private String department;   // 院系/部门
    private String className;    // 班级（仅学生）
    private String major;        // 专业（仅学生）
    private Date createTime;     // 创建时间
    private Date lastLoginTime;  // 最后登录时间
    private boolean isActive;    // 是否激活

    // 构造函数
    public User() {
        this.createTime = new Date();
        this.isActive = true;
    }

    public User(String userId, String password, String realName, Role role) {
        this();
        this.userId = userId;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    // 验证密码
    public boolean validatePassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    // 是否为学生
    public boolean isStudent() {
        return Role.STUDENT.equals(this.role);
    }

    // 是否为教师
    public boolean isTeacher() {
        return Role.TEACHER.equals(this.role);
    }

    // 是否为管理员
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    // 获取显示名称（角色+姓名）
    public String getDisplayName() {
        return (role != null ? role.getDisplayName() : "") + " " +
                (realName != null ? realName : userId);
    }

    // Getter和Setter方法
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, role=%s]", userId, realName, role);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}