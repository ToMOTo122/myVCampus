package com.vcampus.server.service;

import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理服务类
 * 提供用户登录、注册、更新等功能
 */
public class UserService {

    /**
     * 用户登录验证
     */
    public User login(String userId, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE user_id = ? AND password = ? AND is_active = 1";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, password);

            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);

                // 更新最后登录时间
                updateLastLoginTime(userId);

                return user;
            }

        } catch (SQLException e) {
            System.err.println("用户登录查询失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户ID是否已存在
        if (userExists(user.getUserId())) {
            return false;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "INSERT INTO tbl_user (user_id, password, real_name, role, gender, age, " +
                    "email, phone, department, class_name, major, create_time, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 1)";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRealName());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getGender() != null ? user.getGender().name() : null);
            stmt.setInt(6, user.getAge());
            stmt.setString(7, user.getEmail());
            stmt.setString(8, user.getPhone());
            stmt.setString(9, user.getDepartment());
            stmt.setString(10, user.getClassName());
            stmt.setString(11, user.getMajor());

            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("用户注册失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_user SET real_name = ?, gender = ?, age = ?, " +
                    "email = ?, phone = ?, department = ?, class_name = ?, major = ? " +
                    "WHERE user_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getRealName());
            stmt.setString(2, user.getGender() != null ? user.getGender().name() : null);
            stmt.setInt(3, user.getAge());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getDepartment());
            stmt.setString(7, user.getClassName());
            stmt.setString(8, user.getMajor());
            stmt.setString(9, user.getUserId());

            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 修改密码
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        // 先验证旧密码
        User user = login(userId, oldPassword);
        if (user == null) {
            return false;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_user SET password = ? WHERE user_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPassword);
            stmt.setString(2, userId);

            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 根据用户ID查询用户
     */
    public User getUserById(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("查询用户失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * 获取所有用户列表（管理员功能）
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_user ORDER BY create_time DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("查询用户列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return users;
    }

    /**
     * 按角色查询用户
     */
    public List<User> getUsersByRole(User.Role role) {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE role = ? ORDER BY create_time DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, role.name());
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("按角色查询用户失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }

        return users;
    }

    /**
     * 删除用户（软删除）
     */
    public boolean deleteUser(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_user SET is_active = 0 WHERE user_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);

            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 检查用户是否存在
     */
    private boolean userExists(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "SELECT 1 FROM tbl_user WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("检查用户存在性失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseHelper.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseHelper.getConnection();
            String sql = "UPDATE tbl_user SET last_login_time = NOW() WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("更新登录时间失败: " + e.getMessage());
        } finally {
            DatabaseHelper.closeResources(conn, stmt);
        }
    }

    /**
     * 将ResultSet映射为User对象
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));

        // 处理枚举类型
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            user.setRole(User.Role.valueOf(roleStr));
        }

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            user.setGender(User.Gender.valueOf(genderStr));
        }

        user.setAge(rs.getInt("age"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setDepartment(rs.getString("department"));
        user.setClassName(rs.getString("class_name"));
        user.setMajor(rs.getString("major"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        user.setLastLoginTime(rs.getTimestamp("last_login_time"));
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }
}