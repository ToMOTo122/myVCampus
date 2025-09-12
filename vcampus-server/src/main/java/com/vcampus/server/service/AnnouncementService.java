package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告管理服务类
 * 提供公告的发布、查询、管理功能
 */
public class AnnouncementService {

    /**
     * 处理公告管理相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                case ANNOUNCEMENT_LIST:
                    return handleGetAnnouncementList(message);
                case ANNOUNCEMENT_DETAIL:
                    return handleGetAnnouncementDetail(message);
                case ANNOUNCEMENT_ADD:
                    return handleAddAnnouncement(message, currentUser);
                case ANNOUNCEMENT_UPDATE:
                    return handleUpdateAnnouncement(message, currentUser);
                case ANNOUNCEMENT_DELETE:
                    return handleDeleteAnnouncement(message, currentUser);
                default:
                    return Message.error(Message.Code.ERROR, "不支持的公告操作");
            }

        } catch (Exception e) {
            System.err.println("处理公告请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Message.Code.ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取公告列表
     */
    private Message handleGetAnnouncementList(Message message) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 解析查询参数
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

            if (category != null && !category.isEmpty()) {
                sqlBuilder.append("AND category = ? ");
                queryParams.add(category);
            }

            if (targetAudience != null && !targetAudience.isEmpty()) {
                sqlBuilder.append("AND (target_audience = ? OR target_audience = '全体') ");
                queryParams.add(targetAudience);
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

            // 获取总数
            int totalCount = getTotalAnnouncementCount(category, targetAudience);

            Map<String, Object> result = new HashMap<>();
            result.put("announcements", announcements);
            result.put("totalCount", totalCount);
            result.put("currentPage", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (totalCount + pageSize - 1) / pageSize);

            return Message.success(result);

        } catch (SQLException e) {
            System.err.println("获取公告列表失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "获取公告列表失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取公告详情
     */
    private Message handleGetAnnouncementDetail(Message message) {
        Integer announcementId = (Integer) message.getData();
        if (announcementId == null) {
            return Message.error(Message.Code.ERROR, "公告ID不能为空");
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
                announcement.put("createTime", rs.getTimestamp("create_time"));

                return Message.success(announcement);
            } else {
                return Message.error(Message.Code.NOT_FOUND, "公告不存在");
            }

        } catch (SQLException e) {
            System.err.println("获取公告详情失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "获取公告详情失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 添加公告
     */
    private Message handleAddAnnouncement(Message message, User currentUser) {
        // 检查权限
        if (!currentUser.isAdmin()) {
            return Message.error(Message.Code.PERMISSION_DENIED, "只有管理员可以发布公告");
        }

        Map<String, Object> announcementData = (Map<String, Object>) message.getData();
        if (announcementData == null) {
            return Message.error(Message.Code.ERROR, "公告数据不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "INSERT INTO tbl_announcement (title, content, category, priority, " +
                    "author_id, target_audience, is_published, publish_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, (String) announcementData.get("title"));
            stmt.setString(2, (String) announcementData.get("content"));
            stmt.setString(3, (String) announcementData.get("category"));
            stmt.setString(4, (String) announcementData.get("priority"));
            stmt.setString(5, currentUser.getUserId());
            stmt.setString(6, (String) announcementData.get("targetAudience"));
            stmt.setBoolean(7, (Boolean) announcementData.getOrDefault("isPublished", true));
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    return Message.success("公告发布成功，ID: " + newId);
                }
                return Message.success("公告发布成功");
            } else {
                return Message.error(Message.Code.ERROR, "公告发布失败");
            }

        } catch (SQLException e) {
            System.err.println("添加公告失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "添加公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 更新公告
     */
    private Message handleUpdateAnnouncement(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error(Message.Code.PERMISSION_DENIED, "只有管理员可以修改公告");
        }

        Map<String, Object> announcementData = (Map<String, Object>) message.getData();
        if (announcementData == null || !announcementData.containsKey("id")) {
            return Message.error(Message.Code.ERROR, "公告ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_announcement SET title = ?, content = ?, category = ?, " +
                    "priority = ?, target_audience = ?, update_time = ? WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) announcementData.get("title"));
            stmt.setString(2, (String) announcementData.get("content"));
            stmt.setString(3, (String) announcementData.get("category"));
            stmt.setString(4, (String) announcementData.get("priority"));
            stmt.setString(5, (String) announcementData.get("targetAudience"));
            stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(7, (Integer) announcementData.get("id"));

            int result = stmt.executeUpdate();
            if (result > 0) {
                return Message.success("公告更新成功");
            } else {
                return Message.error(Message.Code.NOT_FOUND, "公告不存在或更新失败");
            }

        } catch (SQLException e) {
            System.err.println("更新公告失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "更新公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 删除公告
     */
    private Message handleDeleteAnnouncement(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error(Message.Code.PERMISSION_DENIED, "只有管理员可以删除公告");
        }

        Integer announcementId = (Integer) message.getData();
        if (announcementId == null) {
            return Message.error(Message.Code.ERROR, "公告ID不能为空");
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
                return Message.error(Message.Code.NOT_FOUND, "公告不存在");
            }

        } catch (SQLException e) {
            System.err.println("删除公告失败: " + e.getMessage());
            return Message.error(Message.Code.ERROR, "删除公告失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 获取公告总数
     */
    private int getTotalAnnouncementCount(String category, String targetAudience) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COUNT(*) FROM tbl_announcement WHERE is_published = 1 ");

            List<Object> queryParams = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                sqlBuilder.append("AND category = ? ");
                queryParams.add(category);
            }

            if (targetAudience != null && !targetAudience.isEmpty()) {
                sqlBuilder.append("AND (target_audience = ? OR target_audience = '全体') ");
                queryParams.add(targetAudience);
            }

            stmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < queryParams.size(); i++) {
                stmt.setObject(i + 1, queryParams.get(i));
            }

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("获取公告总数失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return 0;
    }
}