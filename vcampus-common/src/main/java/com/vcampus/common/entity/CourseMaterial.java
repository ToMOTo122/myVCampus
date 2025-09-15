package com.vcampus.common.entity;

import java.io.Serializable;

public class CourseMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    private int materialId;
    private String name;
    private String type;
    private String size;
    private String uploadDate;
    private String filePath;

    public CourseMaterial(int materialId, String name, String type, String size, String uploadDate) {
        this.materialId = materialId;
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
    }

    public CourseMaterial(String name, String type, String size, String uploadDate) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
    }

    public CourseMaterial(String name, String type, String size, String uploadDate, String filePath) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
        this.filePath = filePath;
    }

    // Getter 和 Setter 方法

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) {this.materialId = materialId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
