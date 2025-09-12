package com.vcampus.common.entity;

import java.io.Serializable;

public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String course;
    private String assignDate;
    private String dueDate;
    private String status;
    private String priority;

    public Assignment(String name, String course, String assignDate, String dueDate, String status, String priority) {
        this.name = name;
        this.course = course;
        this.assignDate = assignDate;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
    }

    // Getter 和 Setter 方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getAssignDate() { return assignDate; }
    public void setAssignDate(String assignDate) { this.assignDate = assignDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
