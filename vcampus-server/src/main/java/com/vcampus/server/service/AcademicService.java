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
 * 教务系统CRUD服务类
 * 处理公告和申请的增删改查操作
 */
public class AcademicService {

    /**
     * 处理教务系统相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                // 公告相关操作
                case ANNOUNCEMENT_LIST:
                    return getAnnouncementList(message);
                case ANNOUNCEMENT_DETAIL:
                    return getAnnouncementDetail(message);
                case ANNOUNCEMENT_ADD:
                    return addAnnouncement(message, currentUser);
                case ANNOUNCEMENT_UPDATE:
                    return updateAnnouncement(message, currentUser);
                case ANNOUNCEMENT_DELETE:
                    return deleteAnnouncement(message, currentUser);

                // 申请相关操作
                case APPLICATION_SUBMIT:
                    return submitApplication(message, currentUser);
                case APPLICATION_LIST:
                    return getApplicationList(message, currentUser);
                case APPLICATION_DETAIL:
                    return getApplicationDetail(message, currentUser);
                case APPLICATION_APPROVE:
                    return approveApplication(message, currentUser);
                case APPLICATION_REJECT:
                    return rejectApplication(message, currentUser);

                default:
                    return Message.error("不支持的操作类型");
            }

        } catch (Exception e) {
            System.err.println("处理教务系统请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error("服务器内部错误: " + e.getMessage());
        }
    }

    // ==================== 公告管理 CRUD 操作 ====================

    /**
     * 获取公告列表
     */
    private Message getAnnouncementList(Message message) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Map<String, Object> params = (Map<String, Object>) message.getData();
            String category = params != null ? (String) params.get("category") : null;
            String targetAudience = params != null ? (String) params.get("targetAudience") : null;
            int page = params != null && params.get("page") != null ? (Integer) params.get("page") : 1;
            int pageSize = params != null && params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;

            conn = DatabaseHelper.getConnection();

            // 构建查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT id, title, category, priority, author_id, target_audience, ")
                    .append("publish_date, view_count, ")
                    .append("SUBSTRING(content, 1, 200) as summary ")
                    .append("FROM tbl_announcement WHERE is_published = 1 ");

            List<Object> queryParams = new ArrayList<>();

            if (category != null && !category.isEmpty() && !"全部".equals(category)) {
                sqlBuilder.append("AND category = ? ");
                queryParams.add(category);
            }

            if (targetAudience != null && !targetAudience.isEmpty()) {
                sqlBuilder.append("AND (target_audience = ? OR target_audience = '全体') ");
                if ("STUDENT".equals(targetAudience)) {
                    queryParams.add("学生");
                } else if ("TEACHER".equals(targetAudience)) {
                    queryParams.add("教师");
                } else {
                    queryParams.add("全体");
                }
            }

            sqlBuilder.append("ORDER BY priority DESC, publish_date DESC ");
            sqlBuilder.append("LIMIT ? OFFSET ?");
            queryParams.add(pageSize);
            queryParams.add((page - 1) * pageSize);

            stmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < queryParams.size(); i++) {
                stmt.setObject(i + 1, queryParams.get(i));
            }

            rs = stmt.executeQuery();

            List<Map<String, Object>> announcements = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> announcement = new HashMap<>();
                announcement.put("id", rs.getInt("id"));
                announcement.put("title", rs.getString("title"));
                announcement.put("category", rs.getString("category"));
                announcement.put("priority", rs.getString("priority"));
                announcement.put("authorId", rs.getString("author_id"));
                announcement.put("targetAudience", rs.getString("target_audience"));
                announcement.put("publishDate", rs.getTimestamp("publish_date"));
                announcement.put("viewCount", rs.getInt("view_count"));
                announcement.put("summary", rs.getString("summary"));
                announcements.add(announcement);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("announcements", announcements);
            result.put("currentPage", page);
            result.put("pageSize", pageSize);

            return Message.success(result);

        } catch (SQLException e) {
            System.err.println("获取公告列表失败: " + e.getMessage());
            return Message.error("获取公告列表失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取公告详情
     */
    private Message getAnnouncementDetail(Message message) {
        Integer announcementId = (Integer) message.getData();
        if (announcementId == null) {
            return Message.error("公告ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            // 更新浏览次数
            String updateSql = "UPDATE tbl_announcement SET view_count = view_count + 1 WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, announcementId);
            updateStmt.executeUpdate();
            updateStmt.close();

            // 获取公告详情
            String sql = "SELECT a.*, u.real_name as author_name " +
                    "FROM tbl_announcement a " +
                    "LEFT JOIN tbl_user u ON a.author_id = u.user_id " +
                    "WHERE a.id = ? AND a.is_published = 1";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, announcementId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> announcement = new HashMap<>();
                announcement.put("id", rs.getInt("id"));
                announcement.put("title", rs.getString("title"));
                announcement.put("content", rs.getString("content"));
                announcement.put("category", rs.getString("category"));
                announcement.put("priority", rs.getString("priority"));
                announcement.put("authorId", rs.getString("author_id"));
                announcement.put("authorName", rs.getString("author_name"));
                announcement.put("targetAudience", rs.getString("target_audience"));
                announcement.put("publishDate", rs.getTimestamp("publish_date"));
                announcement.put("viewCount", rs.getInt("view_count"));

                return Message.success(announcement);
            } else {
                return Message.error("公告不存在");
            }

        } catch (SQLException e) {
            System.err.println("获取公告详情失败: " + e.getMessage());
            return Message.error("获取公告详情失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 添加公告
     */
    private Message addAnnouncement(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error("只有管理员可以发布公告");
        }

        Map<String, Object> announcementData = (Map<String, Object>) message.getData();
        if (announcementData == null) {
            return Message.error("公告数据不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "INSERT INTO tbl_announcement (title, content, category, priority, " +
                    "author_id, target_audience, is_published, publish_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, (String) announcementData.get("title"));
            stmt.setString(2, (String) announcementData.get("content"));
            stmt.setString(3, (String) announcementData.get("category"));
            stmt.setString(4, (String) announcementData.get("priority"));
            stmt.setString(5, currentUser.getUserId());
            stmt.setString(6, (String) announcementData.get("targetAudience"));
            stmt.setBoolean(7, (Boolean) announcementData.getOrDefault("isPublished", true));

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("id", newId);
                    responseData.put("message", "公告发布成功");
                    return Message.success(responseData);
                }
                return Message.success("公告发布成功");
            } else {
                return Message.error("公告发布失败");
            }

        } catch (SQLException e) {
            System.err.println("添加公告失败: " + e.getMessage());
            return Message.error("添加公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 更新公告
     */
    private Message updateAnnouncement(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error("只有管理员可以修改公告");
        }

        Map<String, Object> announcementData = (Map<String, Object>) message.getData();
        if (announcementData == null || !announcementData.containsKey("id")) {
            return Message.error("公告ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_announcement SET title = ?, content = ?, category = ?, " +
                    "priority = ?, target_audience = ?, update_time = NOW() WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) announcementData.get("title"));
            stmt.setString(2, (String) announcementData.get("content"));
            stmt.setString(3, (String) announcementData.get("category"));
            stmt.setString(4, (String) announcementData.get("priority"));
            stmt.setString(5, (String) announcementData.get("targetAudience"));
            stmt.setInt(6, (Integer) announcementData.get("id"));

            int result = stmt.executeUpdate();
            if (result > 0) {
                return Message.success("公告更新成功");
            } else {
                return Message.error("公告不存在或更新失败");
            }

        } catch (SQLException e) {
            System.err.println("更新公告失败: " + e.getMessage());
            return Message.error("更新公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 删除公告
     */
    private Message deleteAnnouncement(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error("只有管理员可以删除公告");
        }

        Integer announcementId = (Integer) message.getData();
        if (announcementId == null) {
            return Message.error("公告ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            // 软删除，设置为未发布状态
            String sql = "UPDATE tbl_announcement SET is_published = 0 WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, announcementId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                return Message.success("公告删除成功");
            } else {
                return Message.error("公告不存在");
            }

        } catch (SQLException e) {
            System.err.println("删除公告失败: " + e.getMessage());
            return Message.error("删除公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    // ==================== 申请管理 CRUD 操作 ====================

    /**
     * 提交申请
     */
    private Message submitApplication(Message message, User currentUser) {
        Map<String, Object> applicationData = (Map<String, Object>) message.getData();
        if (applicationData == null) {
            return Message.error("申请数据不能为空");
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
                    "title, content, status, submit_time) VALUES (?, ?, ?, ?, ?, ?, NOW())";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, applicationNo);
            stmt.setString(2, currentUser.getUserId());
            stmt.setString(3, (String) applicationData.get("applicationType"));
            stmt.setString(4, (String) applicationData.get("title"));
            stmt.setString(5, (String) applicationData.get("content"));
            stmt.setString(6, "已提交");

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
            return Message.error("申请提交失败");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("提交申请失败: " + e.getMessage());
            return Message.error("提交申请失败: " + e.getMessage());
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
    private Message getApplicationList(Message message, User currentUser) {
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

            // 如果是查看自己的申请或者是学生
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
            return Message.error("获取申请列表失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取申请详情
     */
    private Message getApplicationDetail(Message message, User currentUser) {
        Integer applicationId = (Integer) message.getData();
        if (applicationId == null) {
            return Message.error("申请ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

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
                if (!currentUser.isAdmin() && !currentUser.isTeacher() && !currentUser.getUserId().equals(applicantId)) {
                    return Message.error("无权查看此申请");
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

                // 获取申请流程
                List<Map<String, Object>> flowSteps = getApplicationFlow(applicationId);
                application.put("flowSteps", flowSteps);

                return Message.success(application);
            } else {
                return Message.error("申请不存在");
            }

        } catch (SQLException e) {
            System.err.println("获取申请详情失败: " + e.getMessage());
            return Message.error("获取申请详情失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 审批通过申请
     */
    private Message approveApplication(Message message, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("无权审批申请");
        }

        Map<String, Object> approvalData = (Map<String, Object>) message.getData();
        if (approvalData == null || !approvalData.containsKey("applicationId")) {
            return Message.error("申请ID不能为空");
        }

        Integer applicationId = (Integer) approvalData.get("applicationId");
        String reviewComment = (String) approvalData.get("reviewComment");

        return updateApplicationStatus(applicationId, currentUser.getUserId(), "已通过", reviewComment);
    }

    /**
     * 拒绝申请
     */
    private Message rejectApplication(Message message, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("无权审批申请");
        }

        Map<String, Object> rejectionData = (Map<String, Object>) message.getData();
        if (rejectionData == null || !rejectionData.containsKey("applicationId")) {
            return Message.error("申请ID不能为空");
        }

        Integer applicationId = (Integer) rejectionData.get("applicationId");
        String reviewComment = (String) rejectionData.get("reviewComment");

        return updateApplicationStatus(applicationId, currentUser.getUserId(), "已拒绝", reviewComment);
    }

    // ==================== 辅助方法 ====================

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
                    "review_time = NOW(), review_comment = ? WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, reviewerId);
            stmt.setString(3, reviewComment);
            stmt.setInt(4, applicationId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                return Message.success("申请" + status);
            } else {
                return Message.error("申请不存在");
            }

        } catch (SQLException e) {
            System.err.println("更新申请状态失败: " + e.getMessage());
            return Message.error("更新申请状态失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 创建申请流程
     */
    private void createApplicationFlow(Connection conn, int applicationId, String applicationType)
            throws SQLException {
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
}