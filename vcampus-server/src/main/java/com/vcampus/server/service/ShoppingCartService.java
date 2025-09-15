//yhr9.14 11：11添加该类
// 文件路径: vcampus-server/src/main/java/com/vcampus/server/service/ShoppingCartService.java
package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Product;
import com.vcampus.common.entity.ShoppingCartItem;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务类
 */
public class ShoppingCartService {

    private final ProductService productService = new ProductService();

    /**
     * 处理购物车相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        String userId = currentUser.getUserId();
        Message.Type type = message.getType();

        try {
            switch (type) {
                case ADD_TO_CART:
                    ShoppingCartItem item = (ShoppingCartItem) message.getData();
                    // 增加错误信息的返回
                    boolean success = addToCart(userId, item.getProductId(), item.getProductNum());
                    return success ? Message.success("成功添加到购物车") : Message.error("加入购物车失败");

                case GET_CART_ITEMS:
                    List<ShoppingCartItem> cartItems = getCartItems(userId);
                    return Message.success(cartItems);

                case GET_CART_COUNT:
                    int count = getCartCount(userId);
                    return Message.success(count);

                case UPDATE_CART_ITEM:
                    ShoppingCartItem updateItem = (ShoppingCartItem) message.getData();
                    boolean updateSuccess = updateCartItem(userId, updateItem.getProductId(), updateItem.getProductNum());
                    return updateSuccess ? Message.success("成功更新购物车商品数量") : Message.error("更新购物车失败");

                case REMOVE_CART_ITEM:
                    String productId = (String) message.getData();
                    boolean removeSuccess = removeCartItem(userId, productId);
                    return removeSuccess ? Message.success("成功移除购物车商品") : Message.error("移除商品失败");

                default:
                    return Message.error("不支持的购物车操作");
            }
        } catch (Exception e) {
            System.err.println("处理购物车请求时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 将商品添加到购物车，如果已存在则更新数量
     */
//    public boolean addToCart(String userId, String productId, int quantity) {
//        Connection conn = null;
//        PreparedStatement pstmt = null;
//        try {
//            conn = DatabaseHelper.getConnection();
//
//            // 1. 验证商品是否存在
//            Product product = productService.getProductById(productId);
//            if (product == null) {
//                System.err.println("尝试添加不存在的商品: " + productId);
//                return false; // 商品不存在，无法添加
//            }
//
//            // 2. 检查购物车中是否已存在该商品
//            String selectSql = "SELECT product_num FROM tbl_shoppingcart WHERE user_id = ? AND product_id = ?";
//            pstmt = conn.prepareStatement(selectSql);
//            pstmt.setString(1, userId);
//            pstmt.setString(2, productId);
//            ResultSet rs = pstmt.executeQuery();
//
//            int currentNum = 0;
//            if (rs.next()) {
//                currentNum = rs.getInt("product_num");
//            }
//            DatabaseHelper.closeResources(null, pstmt, rs);
//
//            if (currentNum > 0) {
//                // 如果商品已存在，则更新数量
//                String updateSql = "UPDATE tbl_shoppingcart SET product_num = product_num + ? WHERE user_id = ? AND product_id = ?";
//                pstmt = conn.prepareStatement(updateSql);
//                pstmt.setInt(1, quantity);
//                pstmt.setString(2, userId);
//                pstmt.setString(3, productId);
//                pstmt.executeUpdate();
//            } else {
//                // 如果商品不存在，则插入新记录
//                String insertSql = "INSERT INTO tbl_shoppingcart (user_id, product_id, product_name, product_price, product_num) VALUES (?, ?, ?, ?, ?)";
//                pstmt = conn.prepareStatement(insertSql);
//                pstmt.setString(1, userId);
//                pstmt.setString(2, productId);
//                pstmt.setString(3, product.getProductName());
//                pstmt.setBigDecimal(4, product.getPrice());
//                pstmt.setInt(5, quantity);
//                pstmt.executeUpdate();
//            }
//            return true;
//        } catch (SQLException e) {
//            System.err.println("数据库操作失败: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        } finally {
//            DatabaseHelper.closeResources(conn, pstmt);
//        }
//    }
    //修改如下 yhr9.14 11：18
    public boolean addToCart(String userId, String productId, int quantity) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // 1. 验证参数和商品存在性
            if (userId == null || productId == null || quantity <= 0) {
                System.err.println("addToCart: 参数无效。");
                return false;
            }

            Product product = productService.getProductById(productId);
            if (product == null) {
                System.err.println("addToCart: 尝试添加不存在的商品，ID: " + productId);
                return false;
            }

            // 2. 检查购物车中是否已存在该商品
            String selectSql = "SELECT product_num FROM tbl_shoppingcart WHERE user_id = ? AND product_id = ?";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            ResultSet rs = pstmt.executeQuery();

            int currentNum = 0;
            if (rs.next()) {
                currentNum = rs.getInt("product_num");
            }
            DatabaseHelper.closeResources(null, pstmt, rs);

            // 3. 执行数据库操作
            if (currentNum > 0) {
                // 商品已存在，执行更新
                String updateSql = "UPDATE tbl_shoppingcart SET product_num = product_num + ? WHERE user_id = ? AND product_id = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, quantity);
                pstmt.setString(2, userId);
                pstmt.setString(3, productId);
                pstmt.executeUpdate();
            } else {
                // 商品不存在，执行插入
                // 为了避免列名顺序问题，直接省略列名
                String insertSql = "INSERT INTO tbl_shoppingcart VALUES (?, ?, ?, ?, ?, null)";
                pstmt = conn.prepareStatement(insertSql);

                // 按照你的数据库列顺序 user_id, product_id, product_name, product_num, product_price, add_time
                pstmt.setString(1, userId);
                pstmt.setString(2, productId);
                pstmt.setString(3, product.getProductName());
                pstmt.setInt(4, quantity);
                pstmt.setBigDecimal(5, product.getPrice());
                // add_time 列由数据库自动生成，我们传 null
                pstmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("addToCart: 数据库操作失败，详细错误信息如下：");
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, pstmt);
        }
    }

    /**
     * 获取用户购物车中的所有商品项
     */
    public List<ShoppingCartItem> getCartItems(String userId) {
        List<ShoppingCartItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_shoppingcart WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProductId(rs.getString("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getBigDecimal("product_price"));
                item.setProductNum(rs.getInt("product_num"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("获取购物车商品失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, pstmt, rs);
        }
        return items;
    }

    /**
     * 获取用户购物车中的商品总数
     */
    public int getCartCount(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT SUM(product_num) AS total_count FROM tbl_shoppingcart WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("total_count");
            }
        } catch (SQLException e) {
            System.err.println("获取购物车总数失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, pstmt, rs);
        }
        return count;
    }

    /**
     * 更新购物车中某件商品的数量
     */
    public boolean updateCartItem(String userId, String productId, int newQuantity) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_shoppingcart SET product_num = ? WHERE user_id = ? AND product_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, userId);
            pstmt.setString(3, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("更新购物车商品失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, pstmt);
        }
    }

    /**
     * 从购物车中移除某件商品
     */
    public boolean removeCartItem(String userId, String productId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseHelper.getConnection();
            String sql = "DELETE FROM tbl_shoppingcart WHERE user_id = ? AND product_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("移除购物车商品失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, pstmt);
        }
    }

    /**
     * 清空购物车
     */
    public void clearCart(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseHelper.getConnection();
            String sql = "DELETE FROM tbl_shoppingcart WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("清空购物车失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, pstmt);
        }
    }
}