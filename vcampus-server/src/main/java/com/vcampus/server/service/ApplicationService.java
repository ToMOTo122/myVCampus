package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请管理服务类
 * 提供在线申请的提交、审批、查询功能
 */
public class ApplicationService {

    /**
     * 处理申请管理相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                case APPLICATION_SUBMIT:
                    return handleSubmitApplication(message, currentUser);
                case APPLICATION_LIST:
                    return handleGetApplicationList(message, currentUser);
                case APPLICATION_DETAIL:
                    return handleGetApplicationDetail(message, currentUser);
                case APPLICATION_APPROVE:
                    return handleApproveApplication(message, currentUser);
                case APPLICATION_REJECT:
                    return handleRejectApplication(message, currentUser);
                default:
                    return Message.error(Message.Code.ERROR, "不支持的申请操作");
            }

        } catch (Exception e) {
            System.err.println("处理申请请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Message.Code.ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 提交申请
     */
    private Message handleSubmitApplication(Message message, User currentUser) {
        Map<String, Object> applicationData = (Map<String, Object>) message.getData();
        if (applicationData == null) {
            return Message.error(Message.Code.ERROR, "申请数据不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // 开始事务

            // 生成申请编号
            String applicationNo = generateApplicationNo();

            // 插入申请记录
            String sql = "INSERT INTO tbl_application (application_no, applicant_id, application_type, " +
                    "title, content, status, submit_time) VALUES (?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, applicationNo);
            stmt.setString(2, currentUser.getUserId());
            stmt.setString(3, (String) applicationData.get("applicationType"));
            stmt.setString(4, (String) applicationData.get("title"));
            stmt.setString(5, (String) applicationData.get("content"));
            stmt.setString(6, "已提交");
            stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int applicationId = generatedKeys.getInt(1);

                    // 创建申请流程
                    createApplicationFlow(conn, applicationId, (String) applicationData.get("applicationType"));

                    conn.commit(); // 提交事务

                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("applicationId", applicationId);
                    responseData.put("applicationNo", applicationNo);
                    responseData.put("message", "申请提交成功");

                    return Message.success(responseData);
                }
            }

            conn.rollback(); // 回滚事务
            return Message.error(Message.Code.ERROR, "申请提交失败");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("提交申请失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "提交申请失败: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 获取申请列表
     */
    private Message handleGetApplicationList(Message message, User currentUser) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Map<String, Object> params = (Map<String, Object>) message.getData();
            String status = params != null ? (String) params.get("status") : null;
            String applicationType = params != null ? (String) params.get("applicationType") : null;
            boolean isMyApplications = params != null ? (Boolean) params.getOrDefault("isMyApplications", false) : false;
            int page = params != null && params.get("page") != null ? (Integer) params.get("page") : 1;
            int pageSize = params != null && params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;

            conn = DatabaseHelper.getConnection();

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT a.*, u.real_name as applicant_name ")
                    .append("FROM tbl_application a ")
                    .append("LEFT JOIN tbl_user u ON a.applicant_id = u.user_id ")
                    .append("WHERE 1=1 ");

            List<Object> queryParams = new ArrayList<>();

            // 如果是查看自己的申请
            if (isMyApplications || currentUser.isStudent()) {
                sqlBuilder.append("AND a.applicant_id = ? ");
                queryParams.add(currentUser.getUserId());
            }

            if (status != null && !status.isEmpty()) {
                sqlBuilder.append("AND a.status = ? ");
                queryParams.add(status);
            }

            if (applicationType != null && !applicationType.isEmpty()) {
                sqlBuilder.append("AND a.application_type = ? ");
                queryParams.add(applicationType);
            }

            sqlBuilder.append("ORDER BY a.submit_time DESC LIMIT ? OFFSET ?");
            queryParams.add(pageSize);
            queryParams.add((page - 1) * pageSize);

            stmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < queryParams.size(); i++) {
                stmt.setObject(i + 1, queryParams.get(i));
            }

            rs = stmt.executeQuery();

            List<Map<String, Object>> applications = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> application = new HashMap<>();
                application.put("id", rs.getInt("id"));
                application.put("applicationNo", rs.getString("application_no"));
                application.put("applicantId", rs.getString("applicant_id"));
                application.put("applicantName", rs.getString("applicant_name"));
                application.put("applicationType", rs.getString("application_type"));
                application.put("title", rs.getString("title"));
                application.put("status", rs.getString("status"));
                application.put("submitTime", rs.getTimestamp("submit_time"));
                application.put("reviewTime", rs.getTimestamp("review_time"));
                applications.add(application);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("applications", applications);
            result.put("currentPage", page);
            result.put("pageSize", pageSize);

            return Message.success(result);

        } catch (SQLException e) {
            System.err.println("获取申请列表失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "获取申请列表失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取申请详情
     */
    private Message handleGetApplicationDetail(Message message, User currentUser) {
        Integer applicationId = (Integer) message.getData();
        if (applicationId == null) {
            return Message.error(Message.Code.ERROR, "申请ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            // 获取申请详情
            String sql = "SELECT a.*, u.real_name as applicant_name, r.real_name as reviewer_name " +
                    "FROM tbl_application a " +
                    "LEFT JOIN tbl_user u ON a.applicant_id = u.user_id " +
                    "LEFT JOIN tbl_user r ON a.reviewer_id = r.user_id " +
                    "WHERE a.id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                // 检查权限
                String applicantId = rs.getString("applicant_id");
                if (!currentUser.isAdmin() && !currentUser.getUserId().equals(applicantId)) {
                    return Message.error(Message.Code.PERMISSION_DENIED, "无权查看此申请");
                }

                Map<String, Object> application = new HashMap<>();
                application.put("id", rs.getInt("id"));
                application.put("applicationNo", rs.getString("application_no"));
                application.put("applicantId", rs.getString("applicant_id"));
                application.put("applicantName", rs.getString("applicant_name"));
                application.put("applicationType", rs.getString("application_type"));
                application.put("title", rs.getString("title"));
                application.put("content", rs.getString("content"));
                application.put("status", rs.getString("status"));
                application.put("submitTime", rs.getTimestamp("submit_time"));
                application.put("reviewerId", rs.getString("reviewer_id"));
                application.put("reviewerName", rs.getString("reviewer_name"));
                application.put("reviewTime", rs.getTimestamp("review_time"));
                application.put("reviewComment", rs.getString("review_comment"));
                application.put("createTime", rs.getTimestamp("create_time"));

                // 获取申请流程
                List<Map<String, Object>> flowSteps = getApplicationFlow(applicationId);
                application.put("flowSteps", flowSteps);

                return Message.success(application);
            } else {
                return Message.error(Message.Code.NOT_FOUND, "申请不存在");
            }

        } catch (SQLException e) {
            System.err.println("获取申请详情失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "获取申请详情失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 审批通过申请
     */
    private Message handleApproveApplication(Message message, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error(Message.Code.PERMISSION_DENIED, "无权审批申请");
        }

        Map<String, Object> approvalData = (Map<String, Object>) message.getData();
        if (approvalData == null || !approvalData.containsKey("applicationId")) {
            return Message.error(Message.Code.ERROR, "申请ID不能为空");
        }

        Integer applicationId = (Integer) approvalData.get("applicationId");
        String reviewComment = (String) approvalData.get("reviewComment");

        return updateApplicationStatus(applicationId, currentUser.getUserId(), "已通过", reviewComment);
    }

    /**
     * 拒绝申请
     */
    private Message handleRejectApplication(Message message, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error(Message.Code.PERMISSION_DENIED, "无权审批申请");
        }

        Map<String, Object> rejectionData = (Map<String, Object>) message.getData();
        if (rejectionData == null || !rejectionData.containsKey("applicationId")) {
            return Message.error(Message.Code.ERROR, "申请ID不能为空");
        }

        Integer applicationId = (Integer) rejectionData.get("applicationId");
        String reviewComment = (String) rejectionData.get("reviewComment");

        return updateApplicationStatus(applicationId, currentUser.getUserId(), "已拒绝", reviewComment);
    }

    /**
     * 更新申请状态
     */
    private Message updateApplicationStatus(Integer applicationId, String reviewerId,
                                            String status, String reviewComment) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_application SET status = ?, reviewer_id = ?, " +
                    "review_time = ?, review_comment = ? WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, reviewerId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, reviewComment);
            stmt.setInt(5, applicationId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                // 发送系统消息通知申请人
                sendNotificationToApplicant(applicationId, status, reviewComment);
                return Message.success("申请" + status);
            } else {
                return Message.error(Message.Code.NOT_FOUND, "申请不存在");
            }

        } catch (SQLException e) {
            System.err.println("更新申请状态失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "更新申请状态失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 创建申请流程
     */
    private void createApplicationFlow(Connection conn, int applicationId, String applicationType)
            throws SQLException {
        // 根据申请类型创建不同的流程步骤
        List<String> steps = getFlowStepsByType(applicationType);

        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO tbl_application_flow (application_id, step_order, step_name, status) " +
                    "VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);

            for (int i = 0; i < steps.size(); i++) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, i + 1);
                stmt.setString(3, steps.get(i));
                stmt.setString(4, i == 0 ? "处理中" : "待处理");
                stmt.addBatch();
            }

            stmt.executeBatch();
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    /**
     * 根据申请类型获取流程步骤
     */
    private List<String> getFlowStepsByType(String applicationType) {
        List<String> steps = new ArrayList<>();

        switch (applicationType) {
            case "成绩证明":
            case "在读证明":
            case "学籍证明":
                steps.add("教务处初审");
                steps.add("开具证明");
                steps.add("完成");
                break;
            case "转专业申请":
                steps.add("院系初审");
                steps.add("教务处审核");
                steps.add("校领导审批");
                steps.add("办理转入手续");
                break;
            case "休学申请":
            case "复学申请":
                steps.add("导师签字");
                steps.add("院系审核");
                steps.add("教务处审批");
                steps.add("完成");
                break;
            default:
                steps.add("初审");
                steps.add("审批");
                steps.add("完成");
                break;
        }

        return steps;
    }

    /**
     * 获取申请流程
     */
    private List<Map<String, Object>> getApplicationFlow(int applicationId) {
        List<Map<String, Object>> flowSteps = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT af.*, u.real_name as handler_name " +
                    "FROM tbl_application_flow af " +
                    "LEFT JOIN tbl_user u ON af.handler_id = u.user_id " +
                    "WHERE af.application_id = ? ORDER BY af.step_order";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> step = new HashMap<>();
                step.put("stepOrder", rs.getInt("step_order"));
                step.put("stepName", rs.getString("step_name"));
                step.put("status", rs.getString("status"));
                step.put("handlerId", rs.getString("handler_id"));
                step.put("handlerName", rs.getString("handler_name"));
                step.put("handleTime", rs.getTimestamp("handle_time"));
                step.put("comment", rs.getString("comment"));
                flowSteps.add(step);
            }

        } catch (SQLException e) {
            System.err.println("获取申请流程失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return flowSteps;
    }

    /**
     * 生成申请编号
     */
    private String generateApplicationNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new java.util.Date());
        long timestamp = System.currentTimeMillis() % 10000;
        return "APP" + dateStr + String.format("%04d", timestamp);
    }

    /**
     * 发送通知给申请人
     */
    private void sendNotificationToApplicant(int applicationId, String status, String reviewComment) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            // 获取申请人信息
            String querySql = "SELECT applicant_id, title FROM tbl_application WHERE id = ?";
            stmt = conn.prepareStatement(querySql);
            stmt.setInt(1, applicationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String applicantId = rs.getString("applicant_id");
                String applicationTitle = rs.getString("title");

                // 插入系统消息
                String insertSql = "INSERT INTO tbl_system_message (recipient_id, title, content, " +
                        "message_type, related_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, applicantId);
                insertStmt.setString(2, "申请审批结果通知");
                insertStmt.setString(3, String.format("您的申请「%s」已%s。%s",
                        applicationTitle, status, reviewComment != null ? "审批意见：" + reviewComment : ""));
                insertStmt.setString(4, "申请回复");
                insertStmt.setInt(5, applicationId);
                insertStmt.executeUpdate();
                insertStmt.close();
            }

        } catch (SQLException e) {
            System.err.println("发送通知失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
}