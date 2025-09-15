// 文件路径：vcampus-server/src/main/java/com/vcampus/server/database/DatabaseConnection.java
package com.vcampus.server.database;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vcampus?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456"; // 修改为你的密码
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static Connection connection = null;

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // 加载数据库驱动
                Class.forName(DB_DRIVER);

                // 建立连接
                connection = DriverManager.getConnection(
                        DB_URL + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false",
                        DB_USER,
                        DB_PASSWORD
                );

                System.out.println("数据库连接建立成功");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("数据库驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        Connection testConn = getConnection();
        if (testConn != null) {
            try {
                return !testConn.isClosed();
            } catch (SQLException e) {
                System.err.println("测试数据库连接时发生错误: " + e.getMessage());
            }
        }
        return false;
    }
}