package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class CardConsumption implements Serializable {
    private static final long serialVersionUID = 1L;

    private int consumptionId;
    private String cardId;
    private double amount;
    private Timestamp time;
    private String location;
    private String type;
    private String remark;

    // Getter和Setter方法
    public int getConsumptionId() { return consumptionId; }
    public void setConsumptionId(int consumptionId) { this.consumptionId = consumptionId; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}