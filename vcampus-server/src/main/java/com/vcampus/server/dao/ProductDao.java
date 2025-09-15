// 文件路径: src/main/java/com/vcampus/server/dao/ProductDao.java
//yhr 9.14 10:14添加该类
// 文件路径: src/main/java/com/vcampus/server/dao/ProductDao.java
// 这是一个完整的、功能正确的版本，请直接替换你现有的文件内容。

package com.vcampus.server.dao;

import com.vcampus.common.entity.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/vcampus?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动加载失败");
            throw new SQLException("MySQL驱动未找到", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * 从数据库中获取所有商品列表
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM tbl_product";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String productId = rs.getString("product_id");
                String productName = rs.getString("product_name");
                String description = rs.getString("description");
                String category = rs.getString("category");
                BigDecimal price = rs.getBigDecimal("product_price");
                int stock = rs.getInt("stock");
                String imageUrl = rs.getString("image_url");

                Product product = new Product(productId, productName, category, price, stock, description, imageUrl);
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("查询商品列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 根据关键字搜索商品
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM tbl_product WHERE product_name LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    String productName = rs.getString("product_name");
                    String description = rs.getString("description");
                    String category = rs.getString("category");
                    BigDecimal price = rs.getBigDecimal("product_price");
                    int stock = rs.getInt("stock");
                    String imageUrl = rs.getString("image_url");

                    Product product = new Product(productId, productName, category, price, stock, description, imageUrl);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("搜索商品失败: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
}