package com.vcampus.client.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * 客户端网络服务类
 * 负责与服务器的Socket通信
 */
public class ClientService {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isConnected = false;

    // 连接配置
    private static final int CONNECTION_TIMEOUT = 5000; // 5秒连接超时
    private static final int READ_TIMEOUT = 10000; // 10秒读取超时

    /**
     * 连接到服务器
     */
    public boolean connect(String host, int port) {
        try {
            System.out.println("正在连接服务器: " + host + ":" + port);

            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), CONNECTION_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);

            // 建立输入输出流
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            isConnected = true;
            System.out.println("服务器连接成功");

            // 接收欢迎消息
            try {
                Message welcomeMessage = (Message) inputStream.readObject();
                System.out.println("收到服务器欢迎消息: " + welcomeMessage.getData());
            } catch (Exception e) {
                System.out.println("接收欢迎消息失败: " + e.getMessage());
            }

            return true;

        } catch (SocketTimeoutException e) {
            System.err.println("连接服务器超时: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送消息到服务器
     */
    public boolean sendMessage(Message message) {
        if (!isConnected || outputStream == null) {
            System.err.println("未连接到服务器");
            return false;
        }

        try {
            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * 接收服务器响应
     */
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        if (!isConnected || inputStream == null) {
            throw new IOException("未连接到服务器");
        }

        return (Message) inputStream.readObject();
    }

    /**
     * 发送消息并等待响应
     */
    // 在ClientService.java中修改这些方法
    public synchronized Message sendAndReceive(Message message) throws IOException, ClassNotFoundException {
        if (sendMessage(message)) {
            return receiveMessage();
        }
        throw new IOException("发送消息失败");
    }

    /**
     * 用户登录
     */
    public User login(User loginUser) {
        try {
            Message loginMessage = new Message(Message.Type.USER_LOGIN, loginUser);
            Message response = sendAndReceive(loginMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (User) response.getData();
            } else {
                System.err.println("登录失败: " + response.getData());
                return null;
            }

        } catch (Exception e) {
            System.err.println("登录请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 用户登出
     */
    public boolean logout() {
        try {
            Message logoutMessage = new Message(Message.Type.USER_LOGOUT, null);
            Message response = sendAndReceive(logoutMessage);

            return response.getCode() == Message.Code.SUCCESS;

        } catch (Exception e) {
            System.err.println("登出请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 用户注册
     */
    public boolean register(User newUser) {
        try {
            Message registerMessage = new Message(Message.Type.USER_REGISTER, newUser);
            Message response = sendAndReceive(registerMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                System.out.println("注册成功: " + response.getData());
                return true;
            } else {
                System.err.println("注册失败: " + response.getData());
                return false;
            }

        } catch (Exception e) {
            System.err.println("注册请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 查询学生列表
     */
    public java.util.List<User> getStudentList() {
        try {
            Message queryMessage = new Message(Message.Type.STUDENT_LIST, null);
            Message response = sendAndReceive(queryMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<User>) response.getData();
            } else {
                System.err.println("查询学生列表失败: " + response.getData());
                return new java.util.ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("查询学生列表请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 查询课程列表
     */
    public java.util.List<?> getCourseList() {
        try {
            Message queryMessage = new Message(Message.Type.COURSE_LIST, null);
            Message response = sendAndReceive(queryMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<?>) response.getData();
            } else {
                System.err.println("查询课程列表失败: " + response.getData());
                return new java.util.ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("查询课程列表请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 搜索图书
     */
    public java.util.List<?> searchBooks(String keyword) {
        try {
            Message searchMessage = new Message(Message.Type.BOOK_SEARCH, keyword);
            Message response = sendAndReceive(searchMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<?>) response.getData();
            } else {
                System.err.println("搜索图书失败: " + response.getData());
                return new java.util.ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("搜索图书请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取商品列表
     */
    public java.util.List<?> getProductList() {
        try {
            Message queryMessage = new Message(Message.Type.SHOP_LIST, null);
            Message response = sendAndReceive(queryMessage);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<?>) response.getData();
            } else {
                System.err.println("查询商品列表失败: " + response.getData());
                return new java.util.ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("查询商品列表请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 通用请求方法
     */
    public synchronized Message request(Message.Type type, Object data) {
        try {
            Message requestMessage = new Message(type, data);
            return sendAndReceive(requestMessage);
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
            return Message.error("请求失败: " + e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            isConnected = false;

            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }

            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

            System.out.println("已断开服务器连接");

        } catch (IOException e) {
            System.err.println("断开连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    public boolean testConnection() {
        if (!isConnected) {
            return false;
        }

        try {
            // 发送心跳消息
            Message heartbeat = new Message(Message.Type.SUCCESS, "heartbeat");
            return sendMessage(heartbeat);
        } catch (Exception e) {
            System.err.println("连接测试失败: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * 启动心跳检测线程
     */
    public void startHeartbeat() {
        if (!isConnected) {
            return;
        }

        Thread heartbeatThread = new Thread(() -> {
            while (isConnected) {
                try {
                    Thread.sleep(30000); // 每30秒发送一次心跳
                    if (!testConnection()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    // Getter方法
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }

    public String getServerAddress() {
        if (socket != null) {
            return socket.getRemoteSocketAddress().toString();
        }
        return "未连接";
    }

    /**
     * 修改后的sendRequest方法 - 使用现有连接而不是创建新连接
     */
    public Message sendRequest(Message request) {
        try {
            // 使用现有的连接发送请求
            if (isConnected()) {
                return sendAndReceive(request);
            } else {
                // 如果连接已断开，返回错误消息
                Message errorResponse = new Message(Message.Type.ERROR, "与服务器连接已断开");
                errorResponse.setCode(Message.Code.ERROR);
                return errorResponse;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // 返回一个包含错误信息的Message对象，以便前端处理
            Message errorResponse = new Message(Message.Type.ERROR, "与服务器通信失败: " + e.getMessage());
            errorResponse.setCode(Message.Code.ERROR);
            return errorResponse;
        }
    }
}