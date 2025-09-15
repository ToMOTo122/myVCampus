//yhr9.14 1:28添加该类
package com.vcampus.common.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class ShoppingCartItem implements Serializable {
    private String userId;
    private String productId;
    private int productNum;
    private String productName; // 冗余存储，便于显示
    private BigDecimal productPrice; // 冗余存储，便于计算总价

    public ShoppingCartItem() {}
    public ShoppingCartItem(String userId, String productId, int productNum) {
        this.userId = userId;
        this.productId = productId;
        this.productNum = productNum;
    }
    public ShoppingCartItem(String userId, String productId, String productName, int productNum, BigDecimal price) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.productNum = productNum;
        this.productPrice = price;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getProductNum() { return productNum; }
    public void setProductNum(int productNum) { this.productNum = productNum; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
}