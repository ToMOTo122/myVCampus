package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Map;

public class Reminder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;

    public Reminder() {}

    public Reminder(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) { this.content = content; }

    public static Reminder fromMap(Map<String, Object> map) {
        Reminder reminder = new Reminder();
        reminder.content = (String) map.get("content");
        return reminder;
    }
}
