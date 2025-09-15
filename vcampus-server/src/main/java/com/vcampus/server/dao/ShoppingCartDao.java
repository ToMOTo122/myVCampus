// 文件路径: src/main/java/com/vcampus/server/dao/ShoppingCartDao.java
//yhr9/14 9：43添加该类
package com.vcampus.server.dao;

import com.vcampus.common.entity.ShoppingCartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartDao {

    // TODO: 替换为你的数据库连接信息
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/vcampus?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL驱动未找到", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * 将商品添加到购物车或更新其数量
     */
    public boolean addOrUpdateItem(ShoppingCartItem item) {
        String sql = "INSERT INTO tbl_shoppingcart (user_id, product_id, product_num) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE product_num = product_num + ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getUserId());
            pstmt.setString(2, item.getProductId());
            pstmt.setInt(3, item.getProductNum());
            pstmt.setInt(4, item.getProductNum()); // 用于更新时的增量

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("添加或更新购物车商品失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取指定用户的购物车商品总数
     */
    public int getCartCount(String userId) {
        String sql = "SELECT SUM(product_num) FROM tbl_shoppingcart WHERE user_id = ?";
        int count = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取购物车数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 获取指定用户的购物车商品列表
     * 这个方法需要 JOIN 商品表来获取商品名称和价格
     */
    public List<ShoppingCartItem> getCartItems(String userId) {
        List<ShoppingCartItem> items = new ArrayList<>();
        // TODO: 确保你的表名和列名与此SQL语句匹配
        String sql = "SELECT c.user_id, c.product_id, c.product_num, " +
                "p.product_name, p.product_price " +
                "FROM tbl_shoppingcart c " +
                "JOIN tbl_product p ON c.product_id = p.product_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    String productName = rs.getString("product_name");
                    BigDecimal price = rs.getBigDecimal("product_price");
                    int productNum = rs.getInt("product_num");

                    ShoppingCartItem item = new ShoppingCartItem(userId, productId, productName, productNum, price);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取购物车商品失败: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 更新购物车中指定商品的数量
     */
    public boolean updateItemQuantity(ShoppingCartItem item) {
        String sql = "UPDATE tbl_shoppingcart SET product_num = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getProductNum());
            pstmt.setString(2, item.getUserId());
            pstmt.setString(3, item.getProductId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("更新购物车商品数量失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从购物车中删除指定商品
     */
    public boolean removeItem(String userId, String productId) {
        String sql = "DELETE FROM tbl_shoppingcart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, productId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("删除购物车商品失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}