//yhr9.14 22:30添加该类
package com.vcampus.common.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class SecondHandItem implements Serializable {
    private int itemId;
    private String studentId;
    private String itemName;
    private String description;
    private BigDecimal price;
    private int stock;
    private String imageUrl;
    private Timestamp postTime;

    public SecondHandItem(int itemId, String studentId, String itemName, String description, BigDecimal price, int stock) {
        this.itemId = itemId;
        this.studentId = studentId;
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public SecondHandItem() {}


    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPostTime(Timestamp postTime) {
        this.postTime = postTime;
    }
}