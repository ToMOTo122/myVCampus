// ============= 修复的CourseSelection.java =============
package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 选课记录实体类
 */
public class CourseSelection implements Serializable {
    private static final long serialVersionUID = 1L;

    // 选课状态枚举
    public enum Status {
        SELECTED("已选择"),
        DROPPED("已退选"),
        COMPLETED("已完成");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String studentId;           // 学生ID
    private String courseId;            // 课程ID
    private Timestamp selectionTime;    // 选课时间 (使用Timestamp而不是LocalDateTime)
    private Status status;              // 选课状态
    private String grade;               // 成绩
    private String remark;              // 备注

    // 构造函数
    public CourseSelection() {
        this.selectionTime = new Timestamp(System.currentTimeMillis());
        this.status = Status.SELECTED;
    }

    public CourseSelection(String studentId, String courseId) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public CourseSelection(String studentId, String courseId, Status status) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
    }

    // Getter 和 Setter 方法
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Timestamp getSelectionTime() {
        return selectionTime;
    }

    public void setSelectionTime(Timestamp selectionTime) {
        this.selectionTime = selectionTime;
    }

    // 支持LocalDateTime到Timestamp的转换
    public void setSelectionTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            this.selectionTime = Timestamp.valueOf(localDateTime);
        }
    }

    // 获取LocalDateTime形式的时间
    public LocalDateTime getSelectionTimeAsLocalDateTime() {
        return selectionTime != null ? selectionTime.toLocalDateTime() : null;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // 工具方法
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "未知";
    }

    public boolean isActive() {
        return Status.SELECTED.equals(status);
    }

    public boolean isDropped() {
        return Status.DROPPED.equals(status);
    }

    public boolean isCompleted() {
        return Status.COMPLETED.equals(status);
    }

    @Override
    public String toString() {
        return "CourseSelection{" +
                "studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", selectionTime=" + selectionTime +
                ", status=" + status +
                ", grade='" + grade + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseSelection that = (CourseSelection) obj;
        return studentId != null ? studentId.equals(that.studentId) &&
                courseId != null ? courseId.equals(that.courseId) : that.courseId == null
                : that.studentId == null;
    }

    @Override
    public int hashCode() {
        int result = studentId != null ? studentId.hashCode() : 0;
        result = 31 * result + (courseId != null ? courseId.hashCode() : 0);
        return result;
    }
}