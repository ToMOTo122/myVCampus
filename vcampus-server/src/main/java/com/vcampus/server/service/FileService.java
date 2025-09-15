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
 * 文件管理服务类
 * 提供文件上传、下载、管理功能
 */
public class FileService {

    /**
     * 处理文件管理相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                case FILE_LIST:
                    return handleGetFileList(message);
                case FILE_UPLOAD:
                    return handleUploadFile(message, currentUser);
                case FILE_DELETE:
                    return handleDeleteFile(message, currentUser);
                case FILE_DOWNLOAD:
                    return handleDownloadFile(message);
                default:
                    return Message.error("不支持的文件操作");
            }

        } catch (Exception e) {
            System.err.println("处理文件请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    private Message handleGetFileList(Message message) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Map<String, Object> params = (Map<String, Object>) message.getData();
            String category = params != null ? (String) params.get("category") : null;

            conn = DatabaseHelper.getConnection();

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT id, file_name, original_name, file_path, file_size, ")
                    .append("category, description, upload_time, uploader_id, download_count ")
                    .append("FROM tbl_files WHERE is_active = 1 ");

            List<Object> queryParams = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                sqlBuilder.append("AND category = ? ");
                queryParams.add(category);
            }

            sqlBuilder.append("ORDER BY upload_time DESC");

            stmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < queryParams.size(); i++) {
                stmt.setObject(i + 1, queryParams.get(i));
            }

            rs = stmt.executeQuery();

            List<Map<String, Object>> files = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> file = new HashMap<>();
                file.put("id", rs.getInt("id"));
                file.put("fileName", rs.getString("file_name"));
                file.put("originalName", rs.getString("original_name"));
                file.put("filePath", rs.getString("file_path"));
                file.put("fileSize", rs.getLong("file_size"));
                file.put("category", rs.getString("category"));
                file.put("description", rs.getString("description"));
                file.put("uploadTime", rs.getTimestamp("upload_time"));
                file.put("uploaderId", rs.getString("uploader_id"));
                file.put("downloadCount", rs.getInt("download_count"));
                files.add(file);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("files", files);

            return Message.success(result);

        } catch (SQLException e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            return Message.error("获取文件列表失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 上传文件
     */
    private Message handleUploadFile(Message message, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("权限不足，只有管理员和教师可以上传文件");
        }

        Map<String, Object> fileData = (Map<String, Object>) message.getData();
        if (fileData == null) {
            return Message.error("文件数据不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "INSERT INTO tbl_files (file_name, original_name, file_path, file_size, " +
                    "category, description, uploader_id, upload_time, is_active, download_count) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 1, 0)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, (String) fileData.get("fileName"));
            stmt.setString(2, (String) fileData.get("originalName"));
            stmt.setString(3, (String) fileData.get("filePath"));
            stmt.setLong(4, (Long) fileData.getOrDefault("fileSize", 0L));
            stmt.setString(5, (String) fileData.get("category"));
            stmt.setString(6, (String) fileData.get("description"));
            stmt.setString(7, currentUser.getUserId());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("fileId", newId);
                    responseData.put("message", "文件上传成功");
                    return Message.success(responseData);
                }
                return Message.success("文件上传成功");
            } else {
                return Message.error("文件上传失败");
            }

        } catch (SQLException e) {
            System.err.println("上传文件失败: " + e.getMessage());
            return Message.error("上传文件失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 删除文件
     */
    private Message handleDeleteFile(Message message, User currentUser) {
        if (!currentUser.isAdmin()) {
            return Message.error("权限不足，只有管理员可以删除文件");
        }

        Integer fileId = (Integer) message.getData();
        if (fileId == null) {
            return Message.error("文件ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            // 软删除
            String sql = "UPDATE tbl_files SET is_active = 0, update_time = NOW() WHERE id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, fileId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                return Message.success("文件删除成功");
            } else {
                return Message.error("文件不存在");
            }

        } catch (SQLException e) {
            System.err.println("删除文件失败: " + e.getMessage());
            return Message.error("删除文件失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 下载文件
     */
    private Message handleDownloadFile(Message message) {
        Integer fileId = (Integer) message.getData();
        if (fileId == null) {
            return Message.error("文件ID不能为空");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            // 更新下载次数
            String updateSql = "UPDATE tbl_files SET download_count = download_count + 1, update_time = NOW() WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, fileId);
            updateStmt.executeUpdate();
            updateStmt.close();

            // 获取文件信息
            String sql = "SELECT file_name, original_name, file_path, file_size, category, description " +
                    "FROM tbl_files WHERE id = ? AND is_active = 1";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, fileId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", rs.getString("file_name"));
                fileInfo.put("originalName", rs.getString("original_name"));
                fileInfo.put("filePath", rs.getString("file_path"));
                fileInfo.put("fileSize", rs.getLong("file_size"));
                fileInfo.put("category", rs.getString("category"));
                fileInfo.put("description", rs.getString("description"));

                return Message.success(fileInfo);
            } else {
                return Message.error("文件不存在");
            }

        } catch (SQLException e) {
            System.err.println("获取文件信息失败: " + e.getMessage());
            return Message.error("获取文件信息失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取文件统计信息
     */
    public Message getFileStatistics(String category) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();

            String sql = "SELECT COUNT(*) as file_count, SUM(file_size) as total_size " +
                    "FROM tbl_files WHERE is_active = 1";

            List<Object> params = new ArrayList<>();
            if (category != null && !category.isEmpty()) {
                sql += " AND category = ?";
                params.add(category);
            }

            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("fileCount", rs.getInt("file_count"));
                statistics.put("totalSize", rs.getLong("total_size"));
                return Message.success(statistics);
            } else {
                return Message.error("获取统计信息失败");
            }

        } catch (SQLException e) {
            System.err.println("获取文件统计失败: " + e.getMessage());
            return Message.error("获取文件统计失败");
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }
}