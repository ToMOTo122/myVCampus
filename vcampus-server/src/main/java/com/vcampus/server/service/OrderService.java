// 文件路径: src/main/java/com/vcampus/server/service/OrderService.java
//yhr 9.14 10：43添加该类
package com.vcampus.server.service;

import com.vcampus.common.entity.*;
import com.vcampus.common.util.DatabaseHelper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务类
 */
public class OrderService {

    private final ProductService productService = new ProductService();
    private final ShoppingCartService shoppingCartService = new ShoppingCartService();

    /**
     * 新增的 handleRequest 方法，作为处理所有订单请求的入口
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();
            String userId = currentUser.getUserId();

            switch (type) {
                case ORDER_LIST:
                    // 获取用户订单列表
                    List<Order> orders = getUserOrders(userId);
                    return Message.success(orders);

                case ORDER_DETAIL:
                    // 获取订单详情
                    Integer orderId = (Integer) message.getData();
                    // 这里你没有 getUserOrderById 方法，所以暂时用这个代替，你应该自己实现它
                    List<OrderItem> orderItems = getOrderItems(orderId);
                    return Message.success(orderItems);

                case ORDER_CANCEL:
                    // TODO: 实现取消订单的逻辑
                    return Message.success("取消订单功能待实现");
                default:
                    return Message.error(Message.Code.ERROR, "不支持的订单操作");
            }
        } catch (Exception e) {
            System.err.println("处理订单请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Message.Code.ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 结账并创建订单
     * @param userId 用户ID
     * @return 订单ID，如果失败返回 -1
     */
    public long checkout(String userId) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 1. 获取购物车商品
            List<ShoppingCartItem> cartItems = shoppingCartService.getCartItems(userId);
            if (cartItems.isEmpty()) {
                return -1;
            }

            // 2. 检查库存并计算总价
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ShoppingCartItem item : cartItems) {
                Product product = productService.getProductById(item.getProductId());
                if (product == null || product.getStock() < item.getProductNum()) {
                    conn.rollback(); // 回滚事务
                    return -1; // 库存不足
                }
                totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductNum())));
            }

            // 3. 创建主订单
            String orderSql = "INSERT INTO tbl_orders (user_id, order_total, status) VALUES (?, ?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, userId);
            orderStmt.setBigDecimal(2, totalAmount);
            orderStmt.setString(3, "paid"); // 假设直接支付
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            long orderId = -1;
            if (rs.next()) {
                orderId = rs.getLong(1);
            } else {
                conn.rollback();
                return -1;
            }
            DatabaseHelper.closeResources(null, orderStmt, rs);

            // 4. 插入订单明细并更新库存
            String itemSql = "INSERT INTO tbl_order_items (order_id, product_id, product_name, product_price, product_num) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            String stockSql = "UPDATE tbl_product SET stock = stock - ? WHERE product_id = ?";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);

            for (ShoppingCartItem item : cartItems) {
                itemStmt.setLong(1, orderId);
                itemStmt.setString(2, item.getProductId());
                itemStmt.setString(3, item.getProductName());
                itemStmt.setBigDecimal(4, item.getProductPrice());
                itemStmt.setInt(5, item.getProductNum());
                itemStmt.addBatch();

                stockStmt.setInt(1, item.getProductNum());
                stockStmt.setString(2, item.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();
            DatabaseHelper.closeResources(null, itemStmt);
            DatabaseHelper.closeResources(null, stockStmt);

            // 5. 清空购物车
            shoppingCartService.clearCart(userId);

            conn.commit(); // 提交事务
            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取用户所有订单
     */
    public List<Order> getUserOrders(String userId) {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_orders WHERE user_id = ? ORDER BY order_time DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getString("user_id"));
                order.setOrderTotal(rs.getBigDecimal("order_total"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                order.setOrderItems(getOrderItems(order.getOrderId())); // 获取订单明细
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        return orders;
    }

    /**
     * 获取单个订单的所有明细项
     */
//    private List<OrderItem> getOrderItems(int orderId) {
//        List<OrderItem> items = new ArrayList<>();
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DatabaseHelper.getConnection();
//            String sql = "SELECT * FROM tbl_order_items WHERE order_id = ?";
//            stmt = conn.prepareStatement(sql);
//            stmt.setInt(1, orderId);
//            rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                OrderItem item = new OrderItem();
//                item.setItemId(rs.getInt("item_id"));
//                item.setOrderId(rs.getString("order_id"));
//                item.setProductId(rs.getString("product_id")); // 修改为getString
//                item.setProductName(rs.getString("product_name"));
//                item.setProductPrice(rs.getBigDecimal("product_price"));
//                item.setProductNum(rs.getInt("product_num"));
//                items.add(item);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            DatabaseHelper.closeResources(conn, stmt, rs);
//        }
//        return items;
//    }
    //更改如下 yhr9.14 11：02
    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_order_items WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setItemId(rs.getInt("item_id"));
                item.setOrderId(rs.getString("order_id"));
                item.setProductId(rs.getString("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getBigDecimal("product_price"));
                item.setProductNum(rs.getInt("product_num"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
        return items;
    }

}