package com.vcampus.server.handler;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Post;
import com.vcampus.common.entity.User;
import com.vcampus.server.VCampusServer;
import com.vcampus.server.service.*;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;

/**
 * 客户端连接处理器
 * 每个客户端连接对应一个处理器实例，运行在独立线程中
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final String clientId;
    private final VCampusServer server;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private boolean isConnected = false;
    private User currentUser = null;
    private Date loginTime;

    // 业务服务
    private final UserService userService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final LibraryService libraryService;
    private final ShopService shopService;
    private final AcademicService academicService;
    private final PostService postService;
    private final EnrollmentService enrollmentService;

    public ClientHandler(Socket clientSocket, String clientId, VCampusServer server) {
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.server = server;

        this.userService = new UserService();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
        this.libraryService = new LibraryService();
        this.shopService = new ShopService();
        this.academicService = new AcademicService();
        this.postService = new PostService();
        this.enrollmentService = new EnrollmentService();
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream  = new ObjectInputStream(clientSocket.getInputStream());

            isConnected = true;
            System.out.println("客户端 " + clientId + " 连接处理器启动");

            sendMessage(new Message(Message.Type.SUCCESS, "欢迎连接到VCampus服务器"));

            while (isConnected) {
                try {
                    Message message = (Message) inputStream.readObject();
                    if (message != null) {
                        System.out.println("收到客户端消息: " + message);
                        Message response = processMessage(message);
                        if (response != null) sendMessage(response);
                    }
                } catch (EOFException e) {
                    break; // 正常断开
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

    /** 统一分发 */
    private Message processMessage(Message message) {
        try {
            Message.Type type = message.getType();
            switch (type) {
                // ===== 用户 =====
                case USER_LOGIN:    return handleLogin(message);
                case USER_LOGOUT:   return handleLogout(message);
                case USER_REGISTER: return handleRegister(message);
                case USER_UPDATE:   return handleUserUpdate(message);

                // ===== 学生 =====
                case STUDENT_ADD:
                case STUDENT_DELETE:
                case STUDENT_UPDATE:
                case STUDENT_QUERY:
                case STUDENT_LIST:
                    return handleStudentOperation(message);

                // ===== 课程 =====
                case COURSE_ADD:
                case COURSE_DELETE:
                case COURSE_UPDATE:
                case COURSE_QUERY:
                case COURSE_LIST:
                case COURSE_SELECT:
                case COURSE_DROP:
                    return handleCourseOperation(message);

                // ===== 图书馆 =====
                case BOOK_SEARCH:
                case BOOK_BORROW:
                case BOOK_RETURN:
                case BOOK_LIST:
                    return handleLibraryOperation(message);

                // ===== 商店 =====
                case SHOP_LIST:
                case SHOP_BUY:
                case SHOP_CART:
                    return handleShopOperation(message);

                // ===== 教务（公告/申请）=====
                case ANNOUNCEMENT_LIST:
                case ANNOUNCEMENT_DETAIL:
                case ANNOUNCEMENT_ADD:
                case ANNOUNCEMENT_UPDATE:
                case ANNOUNCEMENT_DELETE:
                case APPLICATION_SUBMIT:
                case APPLICATION_LIST:
                case APPLICATION_DETAIL:
                case APPLICATION_APPROVE:
                case APPLICATION_REJECT:
                    return handleAcademicOperation(message);

                // ===== 学籍管理（本次新增路由）=====
                case ENROLLMENT_PROFILE_GET:
                case ENROLLMENT_REQUEST_SUBMIT:
                case ENROLLMENT_REQUEST_LIST:
                case ENROLLMENT_REQUEST_DETAIL:
                case ENROLLMENT_REQUEST_APPROVE:
                case ENROLLMENT_REQUEST_REJECT:
                    return handleEnrollmentOperation(message);

                // ===== 论坛 =====
                case POST_GET_ALL:
                case POST_CREATE:
                    return handleForumOperation(message);

                default:
                    return createErrorMessage("不支持的操作类型: " + type);
            }
        } catch (Exception e) {
            System.err.println("处理消息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("服务器内部错误: " + e.getMessage());
        }
    }

    // ====== 用户 ======
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
            }
            return createErrorMessage("用户名或密码错误");
        }
        return createErrorMessage("登录数据格式错误");
    }
    private Message handleLogout(Message message) {
        if (currentUser != null) {
            System.out.println("用户登出: " + currentUser.getDisplayName());
            currentUser = null;
            loginTime = null;
            return createSuccessMessage("登出成功");
        }
        return createErrorMessage("用户未登录");
    }
    private Message handleRegister(Message message) {
        if (message.getData() instanceof User) {
            User newUser = (User) message.getData();
            if (userService.register(newUser)) {
                System.out.println("新用户注册: " + newUser.getUserId());
                return createSuccessMessage("注册成功");
            }
            return createErrorMessage("用户ID已存在");
        }
        return createErrorMessage("注册数据格式错误");
    }
    private Message handleUserUpdate(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        if (message.getData() instanceof User) {
            User updateUser = (User) message.getData();
            if (userService.updateUser(updateUser)) {
                if (currentUser.getUserId().equals(updateUser.getUserId())) currentUser = updateUser;
                return createSuccessMessage("更新成功");
            }
            return createErrorMessage("更新失败");
        }
        return createErrorMessage("更新数据格式错误");
    }

    // ====== 学生/课程/图书/商店/教务 ======
    private Message handleStudentOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) return createErrorMessage("权限不足");
        return studentService.handleRequest(message, currentUser);
    }
    private Message handleCourseOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        return courseService.handleRequest(message, currentUser);
    }
    private Message handleLibraryOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        return libraryService.handleRequest(message, currentUser);
    }
    private Message handleShopOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        return shopService.handleRequest(message, currentUser);
    }
    private Message handleAcademicOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        return academicService.handleRequest(message, currentUser);
    }

    // ====== 学籍（新增）=====
    private Message handleEnrollmentOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录");
        // 具体权限在 EnrollmentService 内部再校验（学生/教师/管理员）
        return enrollmentService.handle(message, currentUser);
    }

    // ====== 论坛 ======
    private Message handleForumOperation(Message message) {
        if (!isLoggedIn()) return createErrorMessage("请先登录再访问论坛");

        Message.Type type = message.getType();
        Message response = new Message();
        switch (type) {
            case POST_GET_ALL: {
                List<Post> posts = postService.getAllPosts();
                response.setType(Message.Type.POST_GET_ALL_SUCCESS);
                response.setData(posts);
                return response;
            }
            case POST_CREATE: {
                Post postToCreate = (Post) message.getData();
                postToCreate.setAuthorName(currentUser.getRealName());
                boolean success = postService.addPost(postToCreate);
                response.setData(success);
                response.setType(success ? Message.Type.POST_CREATE_SUCCESS
                        : Message.Type.POST_CREATE_FAILURE);
                return response;
            }
            default:
                return createErrorMessage("不支持的论坛操作类型: " + type);
        }
    }

    // ====== 基础通用 ======
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
    private void sendErrorMessage(String errorMsg) {
        sendMessage(createErrorMessage(errorMsg));
    }
    private boolean isLoggedIn() { return currentUser != null; }
    private Message createSuccessMessage(Object data) {
        Message message = new Message();
        message.setType(Message.Type.SUCCESS);
        message.setCode(Message.Code.SUCCESS);
        message.setData(data);
        return message;
    }
    private Message createErrorMessage(String errorMsg) {
        Message message = new Message();
        message.setType(Message.Type.ERROR);
        message.setCode(Message.Code.ERROR);
        message.setData(errorMsg);
        return message;
    }

    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭连接时发生错误: " + e.getMessage());
            }
            server.removeClient(clientId);
            if (currentUser != null) {
                System.out.println("用户 " + currentUser.getDisplayName() + " 断开连接");
            }
        }
    }

    public String getClientId() { return clientId; }
    public User getCurrentUser() { return currentUser; }
    public boolean isConnected() { return isConnected; }
    public Date getLoginTime() { return loginTime; }
}
