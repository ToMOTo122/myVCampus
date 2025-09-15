//yhr9.14 22:11添加该类
package com.vcampus.server.dao;

import com.vcampus.common.entity.SecondHandItem;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 二手商品数据访问对象
 * 负责与 tbl_second_hand_item 和 tbl_wanted_item 表进行交互
 */
public class SecondHandDao {

    /**
     * 获取所有二手商品列表
     */
    public List<SecondHandItem> getAllItems() {
        List<SecondHandItem> items = new ArrayList<>();
        String sql = "SELECT * FROM tbl_second_hand_item WHERE stock > 0 ORDER BY post_time DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 根据关键字搜索商品
     */
    public List<SecondHandItem> searchItems(String keyword) {
        List<SecondHandItem> items = new ArrayList<>();
        String sql = "SELECT * FROM tbl_second_hand_item WHERE (item_name LIKE ? OR description LIKE ?) AND stock > 0 ORDER BY post_time DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 发布一个新商品
     */
    public boolean postNewItem(SecondHandItem item) {
        String sql = "INSERT INTO tbl_second_hand_item (student_id, item_name, description, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getStudentId());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setBigDecimal(4, item.getPrice());
            pstmt.setInt(5, item.getStock());
            pstmt.setString(6, item.getImageUrl());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取指定用户发布的商品
     */
    public List<SecondHandItem> getMyPostedItems(String studentId) {
        List<SecondHandItem> items = new ArrayList<>();
        String sql = "SELECT * FROM tbl_second_hand_item WHERE student_id = ? ORDER BY post_time DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 获取指定用户想要（收藏）的商品
     */
    public List<SecondHandItem> getMyWantedItems(String studentId) {
        List<SecondHandItem> items = new ArrayList<>();
        String sql = "SELECT i.* FROM tbl_second_hand_item i JOIN tbl_wanted_item w ON i.item_id = w.item_id WHERE w.student_id = ? AND i.stock > 0 ORDER BY w.wanted_time DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 添加到“想要”列表
     */
    public boolean addWantedItem(String studentId, int itemId) {
        String sql = "INSERT IGNORE INTO tbl_wanted_item (student_id, item_id) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从“想要”列表中移除
     */
    public boolean removeWantedItem(String studentId, int itemId) {
        String sql = "DELETE FROM tbl_wanted_item WHERE student_id = ? AND item_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 辅助方法：将ResultSet映射为SecondHandItem对象
     */
    private SecondHandItem mapResultSetToItem(ResultSet rs) throws SQLException {
        SecondHandItem item = new SecondHandItem();
        item.setItemId(rs.getInt("item_id"));
        item.setStudentId(rs.getString("student_id"));
        item.setItemName(rs.getString("item_name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setStock(rs.getInt("stock"));
        item.setImageUrl(rs.getString("image_url"));
        item.setPostTime(rs.getTimestamp("post_time"));
        return item;
    }
}