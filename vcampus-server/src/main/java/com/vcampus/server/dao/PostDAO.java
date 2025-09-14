package com.vcampus.server.dao; // <--- 请确保这是您服务器端的正确包名

import com.vcampus.common.entity.Post; // 引入我们之前在common模块创建的Post类
import com.vcampus.server.util.DatabaseUtil; // 引入上面的数据库连接工具类

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    /**
     * 从数据库中获取所有帖子, 按时间倒序排列
     * @return 包含所有帖子的列表 (List<Post>)
     */
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT id, title, content, author_name, created_at FROM tbl_post ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setAuthorName(rs.getString("author_name"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all posts from database.");
            e.printStackTrace();
        }
        return posts;
    }

    /**
     * 向数据库中添加一个新帖子
     * @param post 要添加的帖子对象 (不包含ID和创建时间，这些由数据库自动生成)
     * @return 如果添加成功，返回true；否则返回false
     */
    public boolean addPost(Post post) {
        String sql = "INSERT INTO tbl_post(title, content, author_name) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getAuthorName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding new post to database.");
            e.printStackTrace();
            return false;
        }
    }
}