package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 用户实体类
 * 完整版本，包含所有必要的属性和方法
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户角色枚举
    public enum Role {
        ADMIN("管理员"),
        TEACHER("教师"),
        STUDENT("学生");

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
        FEMALE("女"),
        OTHER("其他");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 基本信息
    private String userId;           // 用户ID
    private String password;         // 密码
    private String realName;         // 真实姓名
    private String displayName;      // 显示名称
    private Role role;              // 用户角色
    private Gender gender;          // 性别
    private int age;                // 年龄
    private String email;           // 邮箱
    private String phone;           // 电话

    // 学生/教师特有信息
    private String major;           // 专业
    private String grade;           // 年级/职级
    private String department;      // 院系
    private String className;       // 班级

    // 系统信息
    private Timestamp createTime;        // 创建时间
    private Timestamp lastLoginTime;     // 最后登录时间
    private boolean active;         // 是否激活

    // 构造函数
    public User() {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.active = true;
        this.role = Role.STUDENT; // 默认为学生
        this.age = 0;
    }

    public User(String userId, String password, String realName) {
        this();
        this.userId = userId;
        this.password = password;
        this.realName = realName;
        this.displayName = realName; // 默认显示名称为真实姓名
    }

    public User(String userId, String password, String realName, Role role) {
        this();
        this.userId = userId;
        this.password = password;
        this.realName = realName;
        this.displayName = realName; // 默认显示名称为真实姓名
        this.role = role;
    }

    // Getter 和 Setter 方法
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
        // 如果显示名称为空，使用真实姓名
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = realName;
        }
    }

    public String getDisplayName() {
        // 如果显示名称为空，返回真实姓名
        return displayName != null ? displayName : realName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    // 支持Date类型的设置方法
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = new Timestamp(createTime.getTime());
        }
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    // 支持Date类型的设置方法
    public void setLastLoginTime(Date lastLoginTime) {
        if (lastLoginTime != null) {
            this.lastLoginTime = new Timestamp(lastLoginTime.getTime());
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // 角色判断方法
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isTeacher() {
        return Role.TEACHER.equals(this.role);
    }

    public boolean isStudent() {
        return Role.STUDENT.equals(this.role);
    }

    // 工具方法
    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "未知";
    }

    public String getGenderDisplayName() {
        return gender != null ? gender.getDisplayName() : "未设置";
    }

    /**
     * 获取用户完整信息描述
     */
    public String getFullInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("用户ID: ").append(userId).append("\n");
        sb.append("姓名: ").append(getDisplayName()).append("\n");
        sb.append("角色: ").append(getRoleDisplayName()).append("\n");

        if (isStudent() || isTeacher()) {
            if (department != null) sb.append("院系: ").append(department).append("\n");
            if (major != null) sb.append("专业: ").append(major).append("\n");
            if (grade != null) sb.append("年级: ").append(grade).append("\n");
            if (className != null && isStudent()) sb.append("班级: ").append(className).append("\n");
        }

        if (email != null) sb.append("邮箱: ").append(email).append("\n");
        if (phone != null) sb.append("电话: ").append(phone);

        return sb.toString();
    }

    /**
     * 检查必填字段是否完整
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                realName != null && !realName.trim().isEmpty() &&
                role != null;
    }

    /**
     * 检查是否为新用户（没有登录记录）
     */
    public boolean isNewUser() {
        return lastLoginTime == null;
    }

    /**
     * 获取年龄组
     */
    public String getAgeGroup() {
        if (age <= 0) return "未设置";
        if (age < 18) return "未成年";
        if (age < 30) return "青年";
        if (age < 50) return "中年";
        return "中老年";
    }

    /**
     * 获取用户类型字符串 - 修复LibraryService中的错误
     */
    public String getUserType() {
        if (role == null) {
            return "unknown";
        }

        switch (role) {
            case ADMIN:
                return "admin";
            case TEACHER:
                return "teacher";
            case STUDENT:
                return "student";
            default:
                return "unknown";
        }
    }

    /**
     * 复制用户基本信息（不包括敏感信息如密码）
     */
    public User getSafeUser() {
        User safeUser = new User();
        safeUser.setUserId(this.userId);
        safeUser.setRealName(this.realName);
        safeUser.setDisplayName(this.displayName);
        safeUser.setRole(this.role);
        safeUser.setGender(this.gender);
        safeUser.setAge(this.age);
        safeUser.setEmail(this.email);
        safeUser.setPhone(this.phone);
        safeUser.setMajor(this.major);
        safeUser.setGrade(this.grade);
        safeUser.setDepartment(this.department);
        safeUser.setClassName(this.className);
        safeUser.setCreateTime(this.createTime);
        safeUser.setLastLoginTime(this.lastLoginTime);
        safeUser.setActive(this.active);
        // 不设置密码
        return safeUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", realName='" + realName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", role=" + role +
                ", gender=" + gender +
                ", age=" + age +
                ", major='" + major + '\'' +
                ", grade='" + grade + '\'' +
                ", department='" + department + '\'' +
                ", className='" + className + '\'' +
                ", active=" + active +
                '}';
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