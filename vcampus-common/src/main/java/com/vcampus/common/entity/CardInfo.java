package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class CardInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cardId;
    private String userId;
    private double balance;
    private String status;
    private Timestamp createTime;
    private Timestamp updateTime;

    // Getter和Setter方法
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }
}
