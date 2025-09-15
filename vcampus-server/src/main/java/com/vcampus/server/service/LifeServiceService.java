package com.vcampus.server.service;

import com.vcampus.common.entity.*;
import com.vcampus.common.util.CommonUtils;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LifeServiceService {

    /**
     * 处理一卡通相关请求
     */
    public static Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                case CARD_GET_INFO:
                    return getCardInfo(message, currentUser);
                case CARD_GET_CONSUMPTION:
                    return getConsumptionRecords(message, currentUser);
                case CARD_GET_RECHARGE:
                    return getRechargeRecords(message, currentUser);
                case CARD_RECHARGE:
                    return rechargeCard(message, currentUser);
                case CARD_REPORT_LOSS:
                    return reportCardLoss(message, currentUser);
                case CARD_UNFREEZE:
                    return unfreezeCard(message, currentUser);

                case REPAIR_APPLY:
                    return applyRepair(message, currentUser);
                case REPAIR_GET_LIST:
                    return getRepairList(message, currentUser);
                case REPAIR_GET_DETAIL:
                    return getRepairDetail(message, currentUser);
                case REPAIR_UPDATE_STATUS:
                    return updateRepairStatus(message, currentUser);
                case REPAIR_GET_STATS:
                    return getRepairStats(message, currentUser);

                case LIFE_PAYMENT_GET_BILLS:
                    return getPaymentBills(message, currentUser);
                case LIFE_PAYMENT_PAY:
                    return payBill(message, currentUser);
                case LIFE_PAYMENT_GET_RECORDS:
                    return getPaymentRecords(message, currentUser);

                case CARD_ADD:
                    return addCard(message, currentUser);
                case REPAIR_GET_ALL:
                    return getAllRepairRecords(message, currentUser);
                case REPAIR_ASSIGN_HANDLER:
                    return assignRepairHandler(message, currentUser);
                case LIFE_PAYMENT_ADD_BILL:
                    return addPaymentBill(message, currentUser);
                case USER_SEARCH:
                    return searchUsers(message, currentUser);
                case CARD_GET_ALL:
                    return getAllCards(message, currentUser);
                case REPAIR_COMPLETE:
                    return completeRepair(message, currentUser);

                default:
                    return Message.error("不支持的一卡通操作类型: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error("处理一卡通请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取一卡通信息
     */
    private static Message getCardInfo(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String userId = currentUser.getUserId();
            conn = DatabaseHelper.getConnection();

            // 查询一卡通信息
            String sql = "SELECT * FROM tbl_card WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                CardInfo cardInfo = new CardInfo();
                cardInfo.setCardId(rs.getString("card_id"));
                cardInfo.setUserId(rs.getString("user_id"));
                cardInfo.setBalance(rs.getDouble("balance"));
                cardInfo.setStatus(rs.getString("status"));
                cardInfo.setCreateTime(rs.getTimestamp("create_time"));
                cardInfo.setUpdateTime(rs.getTimestamp("update_time"));

                return Message.success(cardInfo);
            } else {
                return Message.error("未找到该用户的一卡通信息");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取消费记录
     */
    private static Message getConsumptionRecords(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Object[] params = (Object[]) message.getData();
            String cardId = (String) params[0];
            int limit = (Integer) params[1];

            // 验证用户是否拥有此卡
            if (!validateCardOwnership(cardId, currentUser.getUserId())) {
                return Message.error("无权访问此一卡通信息");
            }

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT * FROM tbl_card_consumption WHERE card_id = ? ORDER BY time DESC LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cardId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();

            List<CardConsumption> records = new ArrayList<>();
            while (rs.next()) {
                CardConsumption record = new CardConsumption();
                record.setConsumptionId(rs.getInt("consumption_id"));
                record.setCardId(rs.getString("card_id"));
                record.setAmount(rs.getDouble("amount"));
                record.setTime(rs.getTimestamp("time"));
                record.setLocation(rs.getString("location"));
                record.setType(rs.getString("type"));
                record.setRemark(rs.getString("remark"));

                records.add(record);
            }

            return Message.success(records);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取充值记录
     */
    private static Message getRechargeRecords(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Object[] params = (Object[]) message.getData();
            String cardId = (String) params[0];
            int limit = (Integer) params[1];

            // 验证用户是否拥有此卡
            if (!validateCardOwnership(cardId, currentUser.getUserId())) {
                return Message.error("无权访问此一卡通信息");
            }

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT * FROM tbl_card_recharge WHERE card_id = ? ORDER BY time DESC LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cardId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();

            List<CardRecharge> records = new ArrayList<>();
            while (rs.next()) {
                CardRecharge record = new CardRecharge();
                record.setRechargeId(rs.getInt("recharge_id"));
                record.setCardId(rs.getString("card_id"));
                record.setAmount(rs.getDouble("amount"));
                record.setTime(rs.getTimestamp("time"));
                record.setMethod(rs.getString("method"));
                record.setStatus(rs.getString("status"));

                records.add(record);
            }

            return Message.success(records);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 一卡通充值
     */
    private static Message rechargeCard(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            Object[] params = (Object[]) message.getData();
            String cardId = (String) params[0];
            double amount = (Double) params[1];
            String method = (String) params[2];

            // 转换支付方式为英文枚举值
            String dbMethod;
            switch (method) {
                case "支付宝":
                    dbMethod = "ALIPAY";
                    break;
                case "微信":
                    dbMethod = "WECHAT";
                    break;
                case "银行卡":
                    dbMethod = "BANK_CARD";
                    break;
                case "现金":
                    dbMethod = "CASH";
                    break;
                default:
                    dbMethod = "CASH"; // 默认值
            }

            // 验证用户是否拥有此卡
            if (!validateCardOwnership(cardId, currentUser.getUserId())) {
                return Message.error("无权操作此一卡通");
            }

            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 更新一卡通余额
            String updateSql = "UPDATE tbl_card SET balance = balance + ? WHERE card_id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setDouble(1, amount);
            stmt.setString(2, cardId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                return Message.error("充值失败，一卡通不存在");
            }

            // 插入充值记录，使用转换后的支付方式
            String insertSql = "INSERT INTO tbl_card_recharge (card_id, amount, method, status) VALUES (?, ?, ?, 'SUCCESS')";
            stmt2 = conn.prepareStatement(insertSql);
            stmt2.setString(1, cardId);
            stmt2.setDouble(2, amount);
            stmt2.setString(3, dbMethod); // 使用转换后的支付方式
            stmt2.executeUpdate();

            conn.commit();
            return Message.success("充值成功");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return Message.error("充值失败: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseHelper.closeResources(conn, stmt, null);
            DatabaseHelper.closeResources(null, stmt2, null);
        }
    }

    /**
     * 一卡通挂失
     */
    private static Message reportCardLoss(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            String cardId = (String) message.getData();

            // 验证用户是否拥有此卡
            if (!validateCardOwnership(cardId, currentUser.getUserId())) {
                return Message.error("无权操作此一卡通");
            }

            conn = DatabaseHelper.getConnection();

            String sql = "UPDATE tbl_card SET status = 'LOST' WHERE card_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cardId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("挂失成功");
            } else {
                return Message.error("挂失失败，一卡通不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("挂失失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 一卡通解挂
     */
    private static Message unfreezeCard(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            String cardId = (String) message.getData();

            conn = DatabaseHelper.getConnection();

            String sql = "UPDATE tbl_card SET status = 'NORMAL' WHERE card_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cardId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("解挂成功");
            } else {
                return Message.error("解挂失败，一卡通不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("解挂失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 验证用户是否拥有此一卡通
     */
    private static boolean validateCardOwnership(String cardId, String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            String sql = "SELECT COUNT(*) FROM tbl_card WHERE card_id = ? AND user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cardId);
            stmt.setString(2, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
    /**
     * 提交报修申请
     */
    private static Message applyRepair(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Object[] params = (Object[]) message.getData();
            String title = (String) params[0];
            String description = (String) params[1];
            String location = (String) params[2];
            String priority = (String) params[3];

            conn = DatabaseHelper.getConnection();

            String sql = "INSERT INTO tbl_repair (user_id, title, description, location, priority) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, currentUser.getUserId());
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, location);
            stmt.setString(5, priority);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // 获取生成的ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int repairId = rs.getInt(1);
                    return Message.success(repairId);
                }
                return Message.success("报修申请提交成功");
            } else {
                return Message.error("报修申请提交失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 获取报修列表
     */
    private static Message getRepairList(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String statusFilter = (String) message.getData();

            conn = DatabaseHelper.getConnection();

            String sql;
            if (currentUser.getRole().equals("ADMIN")) {
                // 后勤人员或管理员可以查看所有报修
                if (statusFilter == null || statusFilter.isEmpty()) {
                    sql = "SELECT * FROM tbl_repair ORDER BY create_time DESC";
                    stmt = conn.prepareStatement(sql);
                } else {
                    sql = "SELECT * FROM tbl_repair WHERE status = ? ORDER BY create_time DESC";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, statusFilter);
                }
            } else {
                // 普通用户只能查看自己的报修
                if (statusFilter == null || statusFilter.isEmpty()) {
                    sql = "SELECT * FROM tbl_repair WHERE user_id = ? ORDER BY create_time DESC";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, currentUser.getUserId());
                } else {
                    sql = "SELECT * FROM tbl_repair WHERE user_id = ? AND status = ? ORDER BY create_time DESC";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, currentUser.getUserId());
                    stmt.setString(2, statusFilter);
                }
            }

            rs = stmt.executeQuery();

            List<RepairRecord> records = new ArrayList<>();
            while (rs.next()) {
                RepairRecord record = new RepairRecord();
                record.setRepairId(rs.getInt("repair_id"));
                record.setUserId(rs.getString("user_id"));
                record.setTitle(rs.getString("title"));
                record.setDescription(rs.getString("description"));
                record.setLocation(rs.getString("location"));
                record.setPriority(rs.getString("priority"));
                record.setStatus(rs.getString("status"));
                record.setCreateTime(rs.getTimestamp("create_time"));
                record.setUpdateTime(rs.getTimestamp("update_time"));
                record.setHandler(rs.getString("handler"));
                record.setHandleTime(rs.getTimestamp("handle_time"));
                record.setRemark(rs.getString("remark"));

                records.add(record);
            }

            return Message.success(records);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取报修详情
     */
    private static Message getRepairDetail(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            int repairId = (Integer) message.getData();

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT * FROM tbl_repair WHERE repair_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, repairId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                RepairRecord record = new RepairRecord();
                record.setRepairId(rs.getInt("repair_id"));
                record.setUserId(rs.getString("user_id"));
                record.setTitle(rs.getString("title"));
                record.setDescription(rs.getString("description"));
                record.setLocation(rs.getString("location"));
                record.setPriority(rs.getString("priority"));
                record.setStatus(rs.getString("status"));
                record.setCreateTime(rs.getTimestamp("create_time"));
                record.setUpdateTime(rs.getTimestamp("update_time"));
                record.setHandler(rs.getString("handler"));
                record.setHandleTime(rs.getTimestamp("handle_time"));
                record.setRemark(rs.getString("remark"));

                // 检查权限：用户只能查看自己的报修，后勤人员和管理员可以查看所有
                if (!currentUser.getUserId().equals(record.getUserId()) &  !currentUser.getRole().equals("ADMIN")) {
                    return Message.error("无权查看此报修记录");
                }

                return Message.success(record);
            } else {
                return Message.error("未找到该报修记录");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 更新报修状态
     */
    private static Message updateRepairStatus(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Object[] params = (Object[]) message.getData();
            int repairId = (Integer) params[0];
            String status = (String) params[1];
            String remark = (String) params[2];

            // 检查权限：只有后勤人员和管理员可以更新报修状态
            if (!currentUser.getRole().equals("ADMIN")) {
                return Message.error("无权更新报修状态");
            }

            conn = DatabaseHelper.getConnection();

            String sql;
            if ("已完成".equals(status)) {
                sql = "UPDATE tbl_repair SET status = ?, handler = ?, handle_time = NOW(), remark = ? WHERE repair_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, status);
                stmt.setString(2, currentUser.getDisplayName());
                stmt.setString(3, remark);
                stmt.setInt(4, repairId);
            } else {
                sql = "UPDATE tbl_repair SET status = ?, handler = ?, remark = ? WHERE repair_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, status);
                stmt.setString(2, currentUser.getDisplayName());
                stmt.setString(3, remark);
                stmt.setInt(4, repairId);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("报修状态更新成功");
            } else {
                return Message.error("报修状态更新失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 获取报修统计信息
     */
    private static Message getRepairStats(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 检查权限：只有后勤人员和管理员可以查看统计信息
            if (!currentUser.getRole().equals("ADMIN")) {
                return Message.error("无权查看报修统计");
            }

            conn = DatabaseHelper.getConnection();

            // 获取各状态报修数量
            String sql = "SELECT status, COUNT(*) as count FROM tbl_repair GROUP BY status";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            RepairStats stats = new RepairStats();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");

                switch (status) {
                    case "待处理":
                        stats.setPendingCount(count);
                        break;
                    case "处理中":
                        stats.setProcessingCount(count);
                        break;
                    case "已完成":
                        stats.setCompletedCount(count);
                        break;
                    case "已取消":
                        stats.setCancelledCount(count);
                        break;
                }
            }

            // 获取总报修数
            sql = "SELECT COUNT(*) as total FROM tbl_repair";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalCount(rs.getInt("total"));
            }

            return Message.success(stats);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取用户的生活缴费账单
     */
    private static Message getPaymentBills(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String statusFilter = (String) message.getData(); // 可选参数：待支付、已支付

            conn = DatabaseHelper.getConnection();

            String sql;
            if (statusFilter == null || statusFilter.isEmpty()) {
                sql = "SELECT * FROM tbl_life_payment_bill WHERE user_id = ? ORDER BY due_date DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, currentUser.getUserId());
            } else {
                sql = "SELECT * FROM tbl_life_payment_bill WHERE user_id = ? AND status = ? ORDER BY due_date DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, currentUser.getUserId());
                stmt.setString(2, statusFilter);
            }

            rs = stmt.executeQuery();

            List<LifePaymentBill> bills = new ArrayList<>();
            while (rs.next()) {
                LifePaymentBill bill = new LifePaymentBill();
                bill.setBillId(rs.getInt("bill_id"));
                bill.setUserId(rs.getString("user_id"));
                bill.setBillType(rs.getString("bill_type"));
                bill.setAmount(rs.getDouble("amount"));
                bill.setDueDate(rs.getDate("due_date"));
                bill.setStatus(rs.getString("status"));
                bill.setCreateTime(rs.getTimestamp("create_time"));
                bill.setUpdateTime(rs.getTimestamp("update_time"));

                bills.add(bill);
            }

            return Message.success(bills);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 支付账单
     */
    private static Message payBill(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;

        try {
            Object[] params = (Object[]) message.getData();
            int billId = (Integer) params[0];
            String payMethod = (String) params[1];

            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 1. 获取账单信息
            String getBillSql = "SELECT * FROM tbl_life_payment_bill WHERE bill_id = ? AND user_id = ?";
            stmt = conn.prepareStatement(getBillSql);
            stmt.setInt(1, billId);
            stmt.setString(2, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return Message.error("未找到该账单或无权支付");
            }

            double amount = rs.getDouble("amount");
            String billType = rs.getString("bill_type");

            // 2. 检查账单状态
            if (!"待支付".equals(rs.getString("status"))) {
                conn.rollback();
                return Message.error("该账单已支付或已过期");
            }

            // 3. 如果使用一卡通支付，检查余额并扣款
            if ("一卡通".equals(payMethod)) {
                // 获取一卡通信息
                String getCardSql = "SELECT * FROM tbl_card WHERE user_id = ? AND status = 'NORMAL'";
                stmt2 = conn.prepareStatement(getCardSql);
                stmt2.setString(1, currentUser.getUserId());
                ResultSet cardRs = stmt2.executeQuery();

                if (!cardRs.next()) {
                    conn.rollback();
                    return Message.error("一卡通不存在或已挂失");
                }

                double balance = cardRs.getDouble("balance");
                String cardId = cardRs.getString("card_id");

                if (balance < amount) {
                    conn.rollback();
                    return Message.error("一卡通余额不足");
                }

                // 扣款
                String updateCardSql = "UPDATE tbl_card SET balance = balance - ? WHERE card_id = ?";
                stmt3 = conn.prepareStatement(updateCardSql);
                stmt3.setDouble(1, amount);
                stmt3.setString(2, cardId);
                stmt3.executeUpdate();

                // 记录消费
                String insertConsumptionSql = "INSERT INTO tbl_card_consumption (card_id, amount, location, type) VALUES (?, ?, '生活缴费', ?)";
                stmt3 = conn.prepareStatement(insertConsumptionSql);
                stmt3.setString(1, cardId);
                stmt3.setDouble(2, amount);
                stmt3.setString(3, billType + "缴费");
                stmt3.executeUpdate();
            }

            // 4. 更新账单状态
            String updateBillSql = "UPDATE tbl_life_payment_bill SET status = '已支付' WHERE bill_id = ?";
            stmt2 = conn.prepareStatement(updateBillSql);
            stmt2.setInt(1, billId);
            stmt2.executeUpdate();

            // 5. 添加缴费记录
            String insertRecordSql = "INSERT INTO tbl_life_payment_record (bill_id, user_id, pay_amount, pay_method) VALUES (?, ?, ?, ?)";
            stmt3 = conn.prepareStatement(insertRecordSql);
            stmt3.setInt(1, billId);
            stmt3.setString(2, currentUser.getUserId());
            stmt3.setDouble(3, amount);
            stmt3.setString(4, payMethod);
            stmt3.executeUpdate();

            conn.commit();
            return Message.success("支付成功");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return Message.error("支付失败: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseHelper.closeResources(conn, stmt, null);
            DatabaseHelper.closeResources(null, stmt2, null);
            DatabaseHelper.closeResources(null, stmt3, null);
        }
    }

    /**
     * 获取缴费记录
     */
    private static Message getPaymentRecords(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            int limit = (Integer) message.getData(); // 获取记录条数限制

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT r.*, b.bill_type FROM tbl_life_payment_record r " +
                    "JOIN tbl_life_payment_bill b ON r.bill_id = b.bill_id " +
                    "WHERE r.user_id = ? ORDER BY r.pay_time DESC LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getUserId());
            stmt.setInt(2, limit);

            rs = stmt.executeQuery();

            List<LifePaymentRecord> records = new ArrayList<>();
            while (rs.next()) {
                LifePaymentRecord record = new LifePaymentRecord();
                record.setRecordId(rs.getInt("record_id"));
                record.setBillId(rs.getInt("bill_id"));
                record.setUserId(rs.getString("user_id"));
                record.setPayAmount(rs.getDouble("pay_amount"));
                record.setPayTime(rs.getTimestamp("pay_time"));
                record.setPayMethod(rs.getString("pay_method"));
                record.setBillType(rs.getString("bill_type"));

                records.add(record);
            }

            return Message.success(records);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 添加一卡通
     */
    private static Message addCard(Message message, User currentUser) {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Object[] params = (Object[]) message.getData();
            String userId = (String) params[0];
            String cardId = (String) params[1];

            conn = DatabaseHelper.getConnection();

            // 检查用户是否存在
            String checkUserSql = "SELECT COUNT(*) FROM tbl_user WHERE user_id = ?";
            stmt = conn.prepareStatement(checkUserSql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                return Message.error("用户不存在");
            }

            // 检查是否已有一卡通
            String checkCardSql = "SELECT COUNT(*) FROM tbl_card WHERE user_id = ?";
            stmt = conn.prepareStatement(checkCardSql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return Message.error("该用户已有一卡通");
            }

            // 检查卡号是否已存在
            String checkCardIdSql = "SELECT COUNT(*) FROM tbl_card WHERE card_id = ?";
            stmt = conn.prepareStatement(checkCardIdSql);
            stmt.setString(1, cardId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return Message.error("一卡通号已存在");
            }

            // 添加一卡通
            String insertSql = "INSERT INTO tbl_card (card_id, user_id, balance, status) VALUES (?, ?, 0.00, 'NORMAL')";
            stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, cardId);
            stmt.setString(2, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("一卡通添加成功");
            } else {
                return Message.error("一卡通添加失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 获取所有报修记录
     */
    private static Message getAllRepairRecords(Message message, User currentUser) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String statusFilter = (String) message.getData();

            conn = DatabaseHelper.getConnection();

            String sql;
            if (statusFilter == null || statusFilter.isEmpty()) {
                sql = "SELECT * FROM tbl_repair ORDER BY create_time DESC";
                stmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM tbl_repair WHERE status = ? ORDER BY create_time DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, statusFilter);
            }

            rs = stmt.executeQuery();

            List<RepairRecord> records = new ArrayList<>();
            while (rs.next()) {
                RepairRecord record = new RepairRecord();
                record.setRepairId(rs.getInt("repair_id"));
                record.setUserId(rs.getString("user_id"));
                record.setTitle(rs.getString("title"));
                record.setDescription(rs.getString("description"));
                record.setLocation(rs.getString("location"));
                record.setPriority(rs.getString("priority"));
                record.setStatus(rs.getString("status"));
                record.setCreateTime(rs.getTimestamp("create_time"));
                record.setUpdateTime(rs.getTimestamp("update_time"));
                record.setHandler(rs.getString("handler"));
                record.setHandleTime(rs.getTimestamp("handle_time"));
                record.setRemark(rs.getString("remark"));

                records.add(record);
            }

            return Message.success(records);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 分配维修人员
     */
    private static Message assignRepairHandler(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Object[] params = (Object[]) message.getData();
            int repairId = (Integer) params[0];
            String handler = (String) params[1];

            conn = DatabaseHelper.getConnection();

            String sql = "UPDATE tbl_repair SET handler = ?, status = '处理中', update_time = NOW() WHERE repair_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, handler);
            stmt.setInt(2, repairId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("维修人员分配成功");
            } else {
                return Message.error("维修人员分配失败，报修记录不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 添加缴费账单
     */
    private static Message addPaymentBill(Message message, User currentUser) {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Object[] params = (Object[]) message.getData();
            String userId = (String) params[0];
            String billType = (String) params[1];
            double amount = (Double) params[2];
            Date dueDate = (Date) params[3];

            conn = DatabaseHelper.getConnection();

            // 检查用户是否存在
            String checkUserSql = "SELECT COUNT(*) FROM tbl_user WHERE user_id = ?";
            stmt = conn.prepareStatement(checkUserSql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                return Message.error("用户不存在");
            }

            // 添加缴费账单
            String insertSql = "INSERT INTO tbl_life_payment_bill (user_id, bill_type, amount, due_date, status) VALUES (?, ?, ?, ?, '待支付')";
            stmt = conn.prepareStatement(insertSql);
            stmt.setString(1, userId);
            stmt.setString(2, billType);
            stmt.setDouble(3, amount);
            stmt.setDate(4, new java.sql.Date(dueDate.getTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("缴费账单添加成功");
            } else {
                return Message.error("缴费账单添加失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }

    /**
     * 搜索用户
     */
    private static Message searchUsers(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String keyword = (String) message.getData();

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT user_id, real_name, role, department, class_name FROM tbl_user WHERE user_id LIKE ? OR real_name LIKE ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            rs = stmt.executeQuery();

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setRealName(rs.getString("real_name"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setDepartment(rs.getString("department"));
                user.setClassName(rs.getString("class_name"));

                users.add(user);
            }

            return Message.success(users);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取所有一卡通信息
     */
    private static Message getAllCards(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conn = DatabaseHelper.getConnection();

            String sql = "SELECT * FROM tbl_card ORDER BY create_time DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<CardInfo> cards = new ArrayList<>();
            while (rs.next()) {
                CardInfo cardInfo = new CardInfo();
                cardInfo.setCardId(rs.getString("card_id"));
                cardInfo.setUserId(rs.getString("user_id"));
                cardInfo.setBalance(rs.getDouble("balance"));
                cardInfo.setStatus(rs.getString("status"));
                cardInfo.setCreateTime(rs.getTimestamp("create_time"));
                cardInfo.setUpdateTime(rs.getTimestamp("update_time"));

                cards.add(cardInfo);
            }

            return Message.success(cards);
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库查询失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 完成报修
     */
    private static Message completeRepair(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            int repairId = (Integer) message.getData();


            conn = DatabaseHelper.getConnection();

            String sql = "UPDATE tbl_repair SET status = '已完成', handler = ?, handle_time = NOW() WHERE repair_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getDisplayName());
            stmt.setInt(2, repairId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return Message.success("报修已完成");
            } else {
                return Message.error("报修完成失败，记录不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Message.error("数据库操作失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, null);
        }
    }
}