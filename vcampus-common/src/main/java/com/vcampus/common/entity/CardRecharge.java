package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class CardRecharge implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rechargeId;
    private String cardId;
    private double amount;
    private Timestamp time;
    private String method;
    private String status;

    // Getter和Setter方法
    public int getRechargeId() { return rechargeId; }
    public void setRechargeId(int rechargeId) { this.rechargeId = rechargeId; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
