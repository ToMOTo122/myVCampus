package com.vcampus.server;

import com.vcampus.common.entity.Message;
import com.vcampus.common.util.DatabaseHelper;
import com.vcampus.server.handler.ClientHandler;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

/**
 * VCampus服务器主类
 * 负责监听客户端连接，管理多线程通信
 */
public class VCampusServer {

    // 服务器配置
    private static final int DEFAULT_PORT = 8888;
    private static final int MAX_CLIENTS = 100;

    // 服务器状态
    private ServerSocket serverSocket;
    private int port;
    private boolean isRunning = false;

    // 线程池管理
    private ExecutorService threadPool;

    // 客户端连接管理
    private Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    // 服务器统计信息
    private int totalConnections = 0;
    private Date startTime;

    public VCampusServer() {
        this(DEFAULT_PORT);
    }

    public VCampusServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    /**
     * 启动服务器
     */
    public void start() {
        try {
            // 测试数据库连接
            if (!DatabaseHelper.testConnection()) {
                System.err.println("数据库连接失败，无法启动服务器");
                return;
            }

            serverSocket = new ServerSocket(port);
            isRunning = true;
            startTime = new Date();

            System.out.println("=====================================");
            System.out.println("    VCampus服务器启动成功");
            System.out.println("    监听端口: " + port);
            System.out.println("    启动时间: " + startTime);
            System.out.println("    最大连接数: " + MAX_CLIENTS);
            System.out.println("=====================================");

            // 启动服务器监控线程
            startMonitorThread();

            // 主循环：接受客户端连接
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    totalConnections++;

                    String clientId = "Client_" + totalConnections;
                    String clientAddress = clientSocket.getRemoteSocketAddress().toString();

                    System.out.println("新客户端连接: " + clientId + " 来自 " + clientAddress);

                    // 创建客户端处理器
                    ClientHandler handler = new ClientHandler(clientSocket, clientId, this);
                    activeClients.put(clientId, handler);

                    // 提交到线程池执行
                    threadPool.submit(handler);

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("接受客户端连接时发生错误: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动服务器监控线程
     */
    private void startMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(30000); // 每30秒输出一次状态
                    printServerStatus();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 打印服务器状态
     */
    private void printServerStatus() {
        System.out.println("\n=== 服务器状态 ===");
        System.out.println("运行时间: " + getUptime());
        System.out.println("当前在线客户端: " + activeClients.size());
        System.out.println("总连接数: " + totalConnections);
        System.out.println("线程池状态: " + ((ThreadPoolExecutor)threadPool).getActiveCount() + "/" +
                ((ThreadPoolExecutor)threadPool).getPoolSize());
        System.out.println("================\n");
    }

    /**
     * 获取服务器运行时间
     */
    private String getUptime() {
        if (startTime == null) return "未知";

        long uptimeMs = System.currentTimeMillis() - startTime.getTime();
        long hours = uptimeMs / (1000 * 60 * 60);
        long minutes = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (uptimeMs % (1000 * 60)) / 1000;

        return String.format("%d小时%d分钟%d秒", hours, minutes, seconds);
    }

    /**
     * 移除客户端连接
     */
    public void removeClient(String clientId) {
        ClientHandler handler = activeClients.remove(clientId);
        if (handler != null) {
            System.out.println("客户端断开连接: " + clientId);
        }
    }

    /**
     * 广播消息给所有在线客户端
     */
    public void broadcastMessage(Message message, String excludeClientId) {
        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            if (!entry.getKey().equals(excludeClientId)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    /**
     * 向指定客户端发送消息
     */
    public boolean sendToClient(String clientId, Message message) {
        ClientHandler handler = activeClients.get(clientId);
        if (handler != null) {
            return handler.sendMessage(message);
        }
        return false;
    }

    /**
     * 停止服务器
     */
    public void stop() {
        System.out.println("正在关闭服务器...");
        isRunning = false;

        try {
            // 关闭所有客户端连接
            for (ClientHandler handler : activeClients.values()) {
                handler.disconnect();
            }
            activeClients.clear();

            // 关闭线程池
            threadPool.shutdown();
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }

            // 关闭服务器Socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // 关闭数据库连接池
            DatabaseHelper.closeConnectionPool();

            System.out.println("服务器已关闭");

        } catch (Exception e) {
            System.err.println("关闭服务器时发生错误: " + e.getMessage());
        }
    }

    /**
     * 服务器主入口
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // 解析命令行参数
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("无效的端口号: " + args[0] + "，使用默认端口: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        VCampusServer server = new VCampusServer(port);

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n接收到关闭信号...");
            server.stop();
        }));

        // 启动服务器
        server.start();
    }

    // Getter方法
    public boolean isRunning() { return isRunning; }
    public int getPort() { return port; }
    public int getActiveClientCount() { return activeClients.size(); }
    public int getTotalConnections() { return totalConnections; }
    public Date getStartTime() { return startTime; }
}