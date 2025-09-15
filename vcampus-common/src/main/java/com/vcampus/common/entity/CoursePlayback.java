package com.vcampus.common.entity;

import java.io.Serializable;

public class CoursePlayback implements Serializable {
    private static final long serialVersionUID = 1L;

    private int playbackId;
    private String date;
    private String title;
    private String duration;
    private String videoPath;

    public CoursePlayback(int playbackId, String date, String title, String duration) {
        this.playbackId = playbackId;
        this.date = date;
        this.title = title;
        this.duration = duration;
    }

    public CoursePlayback(String date, String title, String duration) {
        this.date = date;
        this.title = title;
        this.duration = duration;
    }

    public CoursePlayback(String date, String title, String duration, String videoPath) {
        this.date = date;
        this.title = title;
        this.duration = duration;
        this.videoPath = videoPath;
    }

    // Getter 和 Setter 方法

    public int getPlaybackId() { return playbackId; }
    public void setPlaybackId(int playbackId) { this.playbackId = playbackId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
}