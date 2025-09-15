package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Date;

public class BorrowRecord implements Serializable {
    private int recordId;
    private String bookId;
    private String userId;
    private Date borrowDate;
    private Date returnDate;
    private String status;

    public BorrowRecord() {
        // 默认构造函数
    }

    public BorrowRecord(int recordId, String bookId, String userId, Date borrowDate, Date returnDate, String status) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.userId = userId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // getters and setters
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}