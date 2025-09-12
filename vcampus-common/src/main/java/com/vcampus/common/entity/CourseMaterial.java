package com.vcampus.common.entity;

import java.io.Serializable;

public class CourseMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private String size;
    private String uploadDate;

    public CourseMaterial(String name, String type, String size, String uploadDate) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
    }

    // Getter 和 Setter 方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
}
