package com.vcampus.common.util;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

/**
 * 数据库连接工具类
 * 提供数据库连接管理和基础操作
 */
public class DatabaseHelper {
    private static final String CONFIG_FILE = "database.properties";

    // 数据库配置信息
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;

    // 连接池相关
    private static final int MAX_CONNECTIONS = 10;
    private static Connection[] connectionPool = new Connection[MAX_CONNECTIONS];
    private static boolean[] connectionUsed = new boolean[MAX_CONNECTIONS];

    static {
        loadConfig();
        initConnectionPool();
    }

    /**
     * 加载数据库配置
     */
    private static void loadConfig() {
        try (InputStream is = DatabaseHelper.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            Properties props = new Properties();
            if (is != null) {
                props.load(is);
                URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/vcampus");
                USERNAME = props.getProperty("db.username", "root");
                PASSWORD = props.getProperty("db.password", "123456");
                DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            } else {
                // 默认配置
                URL = "jdbc:mysql://localhost:3306/vcampus?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                USERNAME = "root";
                PASSWORD = "123456";
                DRIVER = "com.mysql.cj.jdbc.Driver";
            }

            // 加载驱动
            Class.forName(DRIVER);
            System.out.println("数据库驱动加载成功");

        } catch (Exception e) {
            System.err.println("数据库配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化连接池
     */
    private static void initConnectionPool() {
        try {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                connectionPool[i] = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                connectionUsed[i] = false;
            }
            System.out.println("数据库连接池初始化成功，连接数: " + MAX_CONNECTIONS);
        } catch (SQLException e) {
            System.err.println("数据库连接池初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static synchronized Connection getConnection() throws SQLException {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (!connectionUsed[i]) {
                // 检查连接是否有效
                if (connectionPool[i].isClosed()) {
                    connectionPool[i] = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                }
                connectionUsed[i] = true;
                return connectionPool[i];
            }
        }

        // 如果连接池已满，创建新连接
        System.out.println("连接池已满，创建临时连接");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 释放数据库连接
     */
    public static synchronized void releaseConnection(Connection conn) {
        if (conn == null) return;

        try {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                if (connectionPool[i] == conn) {
                    connectionUsed[i] = false;
                    return;
                }
            }

            // 临时连接直接关闭
            conn.close();
        } catch (SQLException e) {
            System.err.println("释放连接失败: " + e.getMessage());
        }
    }

    /**
     * 关闭资源
     */
    public static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) releaseConnection(conn);
        } catch (SQLException e) {
            System.err.println("关闭资源失败: " + e.getMessage());
        }
    }

    public static void closeResources(Connection conn, PreparedStatement stmt) {
        closeResources(conn, stmt, null);
    }

    /**
     * 执行查询操作
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        // 设置参数
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }

        return stmt.executeQuery();
    }

    /**
     * 执行更新操作（INSERT、UPDATE、DELETE）
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);

            // 设置参数
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        } finally {
            closeResources(conn, stmt);
        }
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                if (connectionPool[i] != null && !connectionPool[i].isClosed()) {
                    connectionPool[i].close();
                }
            }
            System.out.println("数据库连接池已关闭");
        } catch (SQLException e) {
            System.err.println("关闭连接池失败: " + e.getMessage());
        }
    }
}