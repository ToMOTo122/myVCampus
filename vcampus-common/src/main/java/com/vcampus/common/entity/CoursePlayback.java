package com.vcampus.common.entity;

import java.io.Serializable;

public class CoursePlayback implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;
    private String title;
    private String duration;

    public CoursePlayback(String date, String title, String duration) {
        this.date = date;
        this.title = title;
        this.duration = duration;
    }

    // Getter 和 Setter 方法
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}