//yhr9.14 1:25添加该类
package com.vcampus.server.service;

import com.vcampus.common.entity.Product;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务类
 */
public class ProductService {

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_product WHERE stock > 0";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        return products;
    }

    /**
     * 根据关键词搜索商品
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_product WHERE product_name LIKE ? AND stock > 0";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        return products;
    }

    /**
     * 根据ID获取商品
     */
    public Product getProductById(String productId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_product WHERE product_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 更新商品库存
     */
    public boolean updateProductStock(int productId, int quantityChange) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_product SET stock = stock + ? WHERE product_id = ? AND stock + ? >= 0";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantityChange);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 将ResultSet映射为Product对象
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setDescription(rs.getString("description"));
        product.setImageUrl(rs.getString("image_url"));
        return product;
    }
}