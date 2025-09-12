package com.vcampus.server.handler;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.server.VCampusServer;
import com.vcampus.server.service.*;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * 客户端连接处理器
 * 每个客户端连接对应一个处理器实例，运行在独立线程中
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String clientId;
    private VCampusServer server;

    // 输入输出流
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    // 客户端状态
    private boolean isConnected = false;
    private User currentUser = null;
    private Date loginTime;

    // 业务服务类
    private UserService userService;
    private StudentService studentService;
    private CourseService courseService;
    private LibraryService libraryService;
    private ShopService shopService;
    private AcademicService academicService;  // 新增教务系统服务
    private FileService fileService;


    public ClientHandler(Socket clientSocket, String clientId, VCampusServer server) {
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.server = server;

        // 初始化业务服务
        this.userService = new UserService();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
        this.libraryService = new LibraryService();
        this.shopService = new ShopService();
        this.academicService = new AcademicService();  // 初始化教务服务
        this.fileService = new FileService();
    }

    @Override
    public void run() {
        try {
            // 初始化输入输出流
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            isConnected = true;
            System.out.println("客户端 " + clientId + " 连接处理器启动");

            // 发送欢迎消息
            Message welcomeMessage = new Message(Message.Type.SUCCESS, "欢迎连接到VCampus服务器");
            sendMessage(welcomeMessage);

            // 主消息处理循环
            while (isConnected) {
                try {
                    // 接收客户端消息
                    Message message = (Message) inputStream.readObject();

                    if (message != null) {
                        System.out.println("收到客户端消息: " + message);

                        // 处理消息
                        Message response = processMessage(message);

                        // 发送响应
                        if (response != null) {
                            sendMessage(response);
                        }
                    }

                } catch (EOFException e) {
                    // 客户端正常断开连接
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("消息反序列化失败: " + e.getMessage());
                    sendErrorMessage("消息格式错误");
                } catch (IOException e) {
                    if (isConnected) {
                        System.err.println("客户端 " + clientId + " 连接异常: " + e.getMessage());
                    }
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("初始化客户端连接失败: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * 处理客户端消息
     */
    private Message processMessage(Message message) {
        try {
            Message.Type type = message.getType();

            switch (type) {
                // 用户管理
                case USER_LOGIN:
                    return handleLogin(message);
                case USER_LOGOUT:
                    return handleLogout(message);
                case USER_REGISTER:
                    return handleRegister(message);
                case USER_UPDATE:
                    return handleUserUpdate(message);

                // 学生管理
                case STUDENT_ADD:
                case STUDENT_DELETE:
                case STUDENT_UPDATE:
                case STUDENT_QUERY:
                case STUDENT_LIST:
                    return handleStudentOperation(message);

                // 课程管理
                case COURSE_ADD:
                case COURSE_DELETE:
                case COURSE_UPDATE:
                case COURSE_QUERY:
                case COURSE_LIST:
                case COURSE_SELECT:
                case COURSE_DROP:
                    return handleCourseOperation(message);

                // 图书馆管理
                case BOOK_SEARCH:
                case BOOK_BORROW:
                case BOOK_RETURN:
                case BOOK_LIST:
                    return handleLibraryOperation(message);

                // 商店管理
                case SHOP_LIST:
                case SHOP_BUY:
                case SHOP_CART:
                    return handleShopOperation(message);

                // 教务系统 - 公告管理
                case ANNOUNCEMENT_LIST:
                case ANNOUNCEMENT_DETAIL:
                case ANNOUNCEMENT_ADD:
                case ANNOUNCEMENT_UPDATE:
                case ANNOUNCEMENT_DELETE:
                    return handleAcademicOperation(message);

                // 教务系统 - 申请管理
                case APPLICATION_SUBMIT:
                case APPLICATION_LIST:
                case APPLICATION_DETAIL:
                case APPLICATION_APPROVE:
                case APPLICATION_REJECT:
                    return handleAcademicOperation(message);

                // 文件管理
                case FILE_LIST:
                case FILE_UPLOAD:
                case FILE_DELETE:
                case FILE_DOWNLOAD:
                    return handleFileOperation(message);

                default:
                    return createErrorMessage("不支持的操作类型: " + type);
            }

        } catch (Exception e) {
            System.err.println("处理消息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 处理用户登录
     */
    private Message handleLogin(Message message) {
        if (message.getData() instanceof User) {
            User loginUser = (User) message.getData();
            User authenticatedUser = userService.login(loginUser.getUserId(), loginUser.getPassword());

            if (authenticatedUser != null) {
                this.currentUser = authenticatedUser;
                this.loginTime = new Date();
                authenticatedUser.setLastLoginTime(loginTime);

                System.out.println("用户登录成功: " + authenticatedUser.getDisplayName());
                return createSuccessMessage(authenticatedUser);
            } else {
                return createErrorMessage("用户名或密码错误");
            }
        }
        return createErrorMessage("登录数据格式错误");
    }

    /**
     * 处理用户登出
     */
    private Message handleLogout(Message message) {
        if (currentUser != null) {
            System.out.println("用户登出: " + currentUser.getDisplayName());
            currentUser = null;
            loginTime = null;
            return createSuccessMessage("登出成功");
        }
        return createErrorMessage("用户未登录");
    }

    /**
     * 处理用户注册
     */
    private Message handleRegister(Message message) {
        if (message.getData() instanceof User) {
            User newUser = (User) message.getData();

            if (userService.register(newUser)) {
                System.out.println("新用户注册: " + newUser.getUserId());
                return createSuccessMessage("注册成功");
            } else {
                return createErrorMessage("用户ID已存在");
            }
        }
        return createErrorMessage("注册数据格式错误");
    }

    /**
     * 处理用户信息更新
     */
    private Message handleUserUpdate(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        if (message.getData() instanceof User) {
            User updateUser = (User) message.getData();

            if (userService.updateUser(updateUser)) {
                // 如果更新的是当前用户，刷新currentUser
                if (currentUser.getUserId().equals(updateUser.getUserId())) {
                    currentUser = updateUser;
                }
                return createSuccessMessage("更新成功");
            } else {
                return createErrorMessage("更新失败");
            }
        }
        return createErrorMessage("更新数据格式错误");
    }

    /**
     * 处理学生管理相关操作
     */
    private Message handleStudentOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        // 检查权限（只有管理员和教师可以管理学生信息）
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return createErrorMessage("权限不足");
        }

        return studentService.handleRequest(message, currentUser);
    }

    /**
     * 处理课程管理相关操作
     */
    private Message handleCourseOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return courseService.handleRequest(message, currentUser);
    }

    /**
     * 处理图书馆相关操作
     */
    private Message handleLibraryOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return libraryService.handleRequest(message, currentUser);
    }

    /**
     * 处理商店相关操作
     */
    private Message handleShopOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return shopService.handleRequest(message, currentUser);
    }

    /**
     * 处理教务系统相关操作（新增）
     */
    private Message handleAcademicOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return academicService.handleRequest(message, currentUser);
    }

    private Message handleFileOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }
        return fileService.handleRequest(message, currentUser);
    }

    /**
     * 发送消息给客户端
     */
    public boolean sendMessage(Message message) {
        try {
            if (outputStream != null && isConnected) {
                outputStream.writeObject(message);
                outputStream.flush();
                return true;
            }
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            disconnect();
        }
        return false;
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(String errorMsg) {
        sendMessage(createErrorMessage(errorMsg));
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 创建成功消息
     */
    private Message createSuccessMessage(Object data) {
        Message message = new Message();
        message.setType(Message.Type.SUCCESS);
        message.setCode(Message.Code.SUCCESS);
        message.setData(data);
        return message;
    }

    /**
     * 创建错误消息
     */
    private Message createErrorMessage(String errorMsg) {
        Message message = new Message();
        message.setType(Message.Type.ERROR);
        message.setCode(Message.Code.ERROR);
        message.setData(errorMsg);
        return message;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (isConnected) {
            isConnected = false;

            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("关闭连接时发生错误: " + e.getMessage());
            }

            // 从服务器中移除此客户端
            server.removeClient(clientId);

            if (currentUser != null) {
                System.out.println("用户 " + currentUser.getDisplayName() + " 断开连接");
            }
        }
    }

    // Getter方法
    public String getClientId() {
        return clientId;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Date getLoginTime() {
        return loginTime;
    }
}