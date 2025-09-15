//yhr9.14 1:29添加该类
package com.vcampus.common.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItem implements Serializable {
    private int itemId;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal productPrice;
    private int productNum;
    private BigDecimal itemTotal;

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    public int getProductNum() { return productNum; }
    public void setProductNum(int productNum) { this.productNum = productNum; }
    public BigDecimal getItemTotal() { return itemTotal; }
    public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
}