// 文件路径: vcampus-common/src/main/java/com/vcampus/common/entity/Activity.java

package com.vcampus.common.entity;

import java.io.Serializable;

public class Activity implements Serializable {
    private String title;
    private String date;
    private String location;
    private String content;

    public Activity(String title, String date, String location, String content) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.content = content;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        // ListView默认使用toString()方法来显示列表项
        return this.title;
    }
}