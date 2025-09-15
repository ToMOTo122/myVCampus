package com.vcampus.common.entity;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String major;
    private String attendance;

    public Student(String id, String name, String major, String attendance) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.attendance = attendance;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getAttendance() { return attendance; }
    public void setAttendance(String attendance) { this.attendance = attendance; }
}