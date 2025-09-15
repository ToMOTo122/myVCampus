package com.vcampus.common.entity;

import java.util.List;

/**
 * 空间实体类
 */
public class Space {
    private String id;
    private String name;
    private String category; // 一级分类：体育馆、教学楼、图书馆、其他
    private String subCategory; // 二级分类：羽毛球馆、游泳馆等
    private String location;
    private int capacity;
    private String description;
    private List<String> equipment;
    private String image;
    private boolean available;

    // 构造函数
    public Space() {}

    public Space(String id, String name, String category, String subCategory,
                 String location, int capacity, String description,
                 List<String> equipment, String image, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.location = location;
        this.capacity = capacity;
        this.description = description;
        this.equipment = equipment;
        this.image = image;
        this.available = available;
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Space{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", available=" + available +
                '}';
    }
}