//yhr9.14 1:28添加该类
package com.vcampus.common.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    private String productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private int stock;
    private String description;
    private String imageUrl;

    public Product(String productId, String productName, String category, BigDecimal price, int stock, String description, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Product() {}


    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}