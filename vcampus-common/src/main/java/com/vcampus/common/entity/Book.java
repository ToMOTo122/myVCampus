package com.vcampus.common.entity;

import java.io.Serializable;

public class Book implements Serializable {
    private String bookId;
    private String title;
    private String author;
    private String publisher;
    private String status;
    private int stock; // 修复：将类型改为 int

    public Book() {
        // 默认构造函数
    }

    public Book(String bookId, String title, String author, String publisher, String status, int stock) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.status = status;
        this.stock = stock;
    }

    // 修复：生成正确的 getter 和 setter
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", status='" + status + '\'' +
                ", stock=" + stock +
                '}';
    }
}