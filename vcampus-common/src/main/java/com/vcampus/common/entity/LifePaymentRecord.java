package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class LifePaymentRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int recordId;
    private int billId;
    private String userId;
    private double payAmount;
    private Date payTime;
    private String payMethod;
    private String billType;
    private String location;
    private String paymentMethod;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(double payAmount) {
        this.payAmount = payAmount;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }



    public String getPaymentMethod() {
        return paymentMethod;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}