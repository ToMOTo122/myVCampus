package com.vcampus.common.entity; // <--- 注意：包名已经根据您的截图更新为正确的位置

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 论坛帖子数据模型类.
 * 必须实现 Serializable 接口, 这样它才能在网络中被传输.
 */
public class Post implements Serializable {

    private static final long serialVersionUID = 1L; // 用于网络传输的序列化版本ID

    private int id;
    private String title;
    private String content;
    private String authorName;
    private Timestamp createdAt;

    // --- 构造函数 ---
    public Post() {}

    // --- Getter 和 Setter 方法 ---
    // (您可以使用IDEA的快捷键 Alt+Insert 或 右键->Generate->Getter and Setter 来自动生成)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // --- 为了方便在JavaFX的ListView中显示，我们重写toString方法 ---
    @Override
    public String toString() {
        return title + "  (作者: " + authorName + ")";
    }
}