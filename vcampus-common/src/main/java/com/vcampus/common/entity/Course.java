package com.vcampus.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程实体类
 * 用于存储课程的基本信息
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String courseId;          // 课程编号
    private String courseName;        // 课程名称
    private String teacherId;         // 教师编号
    private String teacherName;       // 教师姓名
    private String description;       // 课程描述
    private int credits;              // 学分
    private int capacity;             // 课程容量
    private int enrolled;             // 已选人数
    private String schedule;          // 上课时间
    private String classroom;         // 教室
    private String semester;          // 学期
    private String status;            // 状态（开放选课、已满员、已结束等）
    private String department;        // 开课院系
    private String courseType;        // 课程类型（必修、选修等）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private String className; //在线课程功能
    private String startDate; //在线课程功能
    private String endDate;   //在线课程功能
    private int studentCount;  //在线课堂功能

    // 确保有对应的getter和setter方法
    // 构造函数
    public Course() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.enrolled = 0;
        this.status = "开放选课";
    }

    public Course(String courseId, String courseName, String teacherId, String teacherName,
                  int credits, int capacity, String schedule, String classroom, String semester) {
        this();
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.credits = credits;
        this.capacity = capacity;
        this.schedule = schedule;
        this.classroom = classroom;
        this.semester = semester;
    }
    public Course(String courseName, String className, String teacherName, String startDate, String endDate, String status, int credits) {
        this.courseName = courseName;
        this.className = className;
        this.teacherName = teacherName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.credits = credits;
    }
    // Getter 和 Setter 方法
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled = enrolled;
        updateStatus();
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public String getStartDate() { return startDate; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    /**
     * 根据选课人数自动更新状态
     */
    private void updateStatus() {
        if (enrolled >= capacity) {
            this.status = "已满员";
        } else if (enrolled == 0) {
            this.status = "开放选课";
        } else {
            this.status = "选课中";
        }
    }

    /**
     * 检查是否还有选课名额
     */
    public boolean hasCapacity() {
        return enrolled < capacity;
    }

    /**
     * 获取剩余选课名额
     */
    public int getRemainingCapacity() {
        return capacity - enrolled;
    }

    /**
     * 增加选课人数
     */
    public boolean addEnrollment() {
        if (hasCapacity()) {
            this.enrolled++;
            updateStatus();
            this.updateTime = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 减少选课人数
     */
    public boolean removeEnrollment() {
        if (enrolled > 0) {
            this.enrolled--;
            updateStatus();
            this.updateTime = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 获取选课率（百分比）
     */
    public double getEnrollmentRate() {
        if (capacity == 0) return 0.0;
        return (double) enrolled / capacity * 100;
    }

    /**
     * 格式化显示课程信息
     */
    public String getFormattedInfo() {
        return String.format("%s - %s (%s学分)", courseId, courseName, credits);
    }

    /**
     * 获取详细的课程描述
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("课程编号: ").append(courseId).append("\n");
        sb.append("课程名称: ").append(courseName).append("\n");
        sb.append("任课教师: ").append(teacherName).append("\n");
        sb.append("学分: ").append(credits).append("\n");
        sb.append("上课时间: ").append(schedule != null ? schedule : "待定").append("\n");
        sb.append("教室: ").append(classroom != null ? classroom : "待定").append("\n");
        sb.append("选课人数: ").append(enrolled).append("/").append(capacity).append("\n");
        sb.append("状态: ").append(status);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", credits=" + credits +
                ", enrolled=" + enrolled +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseId != null ? courseId.equals(course.courseId) : course.courseId == null;
    }

    @Override
    public int hashCode() {
        return courseId != null ? courseId.hashCode() : 0;
    }
}