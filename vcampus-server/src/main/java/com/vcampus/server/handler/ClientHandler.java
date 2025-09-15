// ============= 修复后的ClientHandler.java =============
package com.vcampus.server.handler;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.server.VCampusServer;
import com.vcampus.server.service.*;
import com.vcampus.server.database.DatabaseConnection;
import com.vcampus.server.service.OrderService;
import com.vcampus.server.service.SecondHandService;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.util.Date;
import com.vcampus.server.service.*;

/**
 * 客户端连接处理器
 * 每个客户端连接对应一个处理器实例，运行在独立线程中
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String clientId;
    private VCampusServer server;

    //yhr9.14 22:14添加
    // 在 ClientHandler 的顶部添加这个服务类实例
    private SecondHandService secondhandService;

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
    private AcademicService academicService;
    private FileService fileService;

    //yhr9.14 10:29添加
    private OnlineClassService onlineClassService; // 新增在线课程服务
    private OrderService orderService; // 新增订单服务
    private AnnouncementService announcementService; // 新增公告服务
    private ApplicationService applicationService; // 新增申请服务
    private ProductService productService; // 新增商品服务
    private ShoppingCartService shoppingCartService; // 新增购物车服务


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

        //yhr9.14 10:29添加
        //this.onlineClassService = new OnlineClassService(); // 初始化
        this.orderService = new OrderService(); // 初始化
        this.announcementService = new AnnouncementService(); // 初始化
        this.applicationService = new ApplicationService(); // 初始化
        this.productService = new ProductService(); // 初始化
        this.shoppingCartService = new ShoppingCartService(); // 初始化
    }

    /**
     * 初始化所有服务类
     */
//    private void initializeServices() {
//        try {
//            // 获取数据库连接
//            Connection connection = DatabaseConnection.getConnection();
//
//            if (connection != null) {
//                // 使用数据库连接初始化服务
//                this.courseService = new CourseService(connection);
//                this.userService = new UserService(connection);
//                this.studentService = new StudentService(connection);
//                this.libraryService = new LibraryService(connection);
//                this.shopService = new ShopService();
//                this.academicService = new AcademicService(connection);
//
//                System.out.println("所有服务初始化成功");
//            } else {
//                System.err.println("数据库连接为空，使用默认构造函数初始化服务");
//                // 如果数据库连接失败，使用默认构造函数
//                this.courseService = new CourseService();
//                this.userService = new UserService();
//                this.studentService = new StudentService();
//                this.libraryService = new LibraryService();
//                this.shopService = new ShopService();
//                this.academicService = new AcademicService();
//            }
//        } catch (Exception e) {
//            System.err.println("初始化服务时发生错误: " + e.getMessage());
//            e.printStackTrace();
//
//            // 使用默认构造函数作为后备
//            this.courseService = new CourseService();
//            this.userService = new UserService();
//            this.studentService = new StudentService();
//            this.libraryService = new LibraryService();
//            this.shopService = new ShopService();
//            this.academicService = new AcademicService();
//        }
//    }

    @Override
    public void run() {
        try {
            // 初始化输入输出流
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            isConnected = true;
            System.out.println("客户端 " + clientId + " 连接处理器启动");

            // 发送欢迎消息
            Message welcomeMessage = new Message(Message.Type.SUCCESS, Message.Code.SUCCESS, "欢迎连接到VCampus服务器");
            sendMessage(welcomeMessage);

            // 主消息处理循环
            while (isConnected) {
                try {
                    // 接收客户端消息
                    Message message = (Message) inputStream.readObject();

                    if (message != null) {
                        System.out.println("收到客户端消息: " + message.getType());

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

            System.out.println("处理消息类型: " + type + ", 当前用户: " +
                    (currentUser != null ? currentUser.getDisplayName() : "未登录"));

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

                // 课程管理 - 修复：确保所有课程相关消息都被处理
                case COURSE_ADD:
                case COURSE_DELETE:
                case COURSE_UPDATE:
                case COURSE_QUERY:
                case COURSE_LIST:
                case COURSE_SELECT:
                case COURSE_DROP:
                case STUDENT_SELECTED_COURSES:
                case TEACHER_COURSES:
                case TEACHER_LIST:
                case SELECT_COURSE:
                case DROP_COURSE:
                case ADD_COURSE:
                case UPDATE_COURSE:
                case DELETE_COURSE:
                case COURSE_SELECTIONS:  // 添加这个关键类型
                case USER_LIST:          // 添加这个类型
                    return handleCourseOperation(message);

                // 图书馆管理
                case BOOK_SEARCH:
                case BOOK_BORROW:
                case BOOK_RETURN:
                case BOOK_LIST:
                case BOOK_ADD:
                case BOOK_DELETE:
                case BOOK_UPDATE:
                case BOOK_RENEW:
                case BORROW_HISTORY:
                case BORROW_RECORD_LIST:
                case BORROW_RECORD_GET_ALL:
                case BORROW_RECORD_RETURN:
                case BOOK_GET_ALL:
                    return handleLibraryOperation(message);

                // 商店管理
                case SHOP_LIST:
                case SEARCH_PRODUCT:
                case ADD_TO_CART:
                case GET_CART_COUNT:
                case GET_CART_ITEMS:
                case UPDATE_CART_ITEM:
                case REMOVE_CART_ITEM:
                case CHECKOUT:
                    return shopService.handleRequest(message, currentUser);

                //yhr9.14 22:16添加
                // 在 processMessage 方法的 switch 语句中，添加如下代码块：
                case SECOND_HAND_LIST:
                case SECOND_HAND_SEARCH:
                case SECOND_HAND_POST:
                case SECOND_HAND_MY_POSTS:
                case SECOND_HAND_WANT:
                case SECOND_HAND_MY_WANTS:
                case SECOND_HAND_REMOVE_WANT:
                    return secondhandService.handleRequest(message, currentUser);

                // 订单管理
                case ORDER_LIST:
                case ORDER_DETAIL:
                case ORDER_CANCEL:
                    return orderService.handleRequest(message, currentUser);

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

                // 在线课堂相关请求处理
                case ONLINE_CLASS_GET_TEACHER_TODOS:
                case ONLINE_CLASS_GET_TODAY_REMINDERS:
                case ONLINE_CLASS_GET_MONTH_REMINDERS:
                case ONLINE_CLASS_GET_DAY_REMINDERS:
                case ONLINE_CLASS_GET_STUDENT_COURSES:
                case ONLINE_CLASS_SEARCH_COURSES:
                case ONLINE_CLASS_GET_COURSE_STATS:
                case ONLINE_CLASS_GET_COURSE_MATERIALS:
                case ONLINE_CLASS_GET_COURSE_PLAYBACKS:
                case ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS:
                case ONLINE_CLASS_SEARCH_ASSIGNMENTS:
                case ONLINE_CLASS_GET_ASSIGNMENT_STATS:
                case ONLINE_CLASS_SUBMIT_ASSIGNMENT:
                case ONLINE_CLASS_GET_RECENT_ACTIVITIES:
                case ONLINE_CLASS_ADD_REMINDER:
                case ONLINE_CLASS_GET_DISCUSSIONS:
                case ONLINE_CLASS_POST_DISCUSSION:
                case ONLINE_CLASS_GET_TEACHER_COURSES:
                case ONLINE_CLASS_GET_TEACHER_COURSE_STATS:
                case ONLINE_CLASS_GET_CLASS_NAMES:
                case ONLINE_CLASS_ADD_COURSE:
                case ONLINE_CLASS_ADD_PLAYBACK:
                case ONLINE_CLASS_ADD_MATERIAL:
                case ONLINE_CLASS_GET_TEACHER_ASSIGNMENTS:
                case ONLINE_CLASS_DELETE_MATERIAL:
                case ONLINE_CLASS_DELETE_PLAYBACK:
                case ONLINE_CLASS_GET_COURSE_DISCUSSIONS:
                case ONLINE_CLASS_REPLY_TO_DISCUSSION:
                case ONLINE_CLASS_PUBLISH_ASSIGNMENT:
                case ONLINE_CLASS_GRADE_ASSIGNMENT:
                case ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS_FOR_TEACHER:
                case ONLINE_CLASS_GET_COURSE_STUDENTS:
                case ONLINE_CLASS_GET_TEACHER_COURSES_FOR_ASSIGNMENT:
                case ONLINE_CLASS_GET_ASSIGNMENT_DETAILS:
                case ONLINE_CLASS_SEARCH_TEACHER_ASSIGNMENTS:
                case ONLINE_CLASS_GET_TEACHER_COURSE_NAMES:
                case ONLINE_CLASS_SEARCH_TEACHER_COURSES:
                case ONLINE_CLASS_GET_ASSIGNMENT_FEEDBACK:
                    return handleOnlineClassOperation(message);

                case CARD_GET_INFO:
                case CARD_GET_CONSUMPTION:
                case CARD_GET_RECHARGE:
                case CARD_RECHARGE:
                case CARD_REPORT_LOSS:
                case CARD_UNFREEZE:
                case REPAIR_APPLY:
                case REPAIR_GET_LIST:
                case REPAIR_GET_DETAIL:
                case REPAIR_UPDATE_STATUS:
                case REPAIR_GET_STATS:
                case LIFE_PAYMENT_GET_BILLS:
                case LIFE_PAYMENT_PAY:
                case LIFE_PAYMENT_GET_RECORDS:
                case CARD_ADD:
                case REPAIR_GET_ALL:
                case REPAIR_ASSIGN_HANDLER:
                case LIFE_PAYMENT_ADD_BILL:
                case USER_SEARCH:
                case CARD_GET_ALL:
                case REPAIR_COMPLETE:
                    return handleLifeServiceOperation(message);

                default:
                    System.err.println("未处理的消息类型: " + type);
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

            // 修复：确保userService有handleRequest方法或直接调用login方法
            try {
                // 如果userService有handleRequest方法，使用它
                if (userService != null) {
                    Message response = userService.handleRequest(message, null);
                    if (response.getCode() == Message.Code.SUCCESS && response.getData() instanceof User) {
                        this.currentUser = (User) response.getData();
                        this.loginTime = new Date();
                        System.out.println("用户登录成功: " + currentUser.getDisplayName());
                    }
                    return response;
                }
            } catch (Exception e) {
                System.err.println("登录处理异常: " + e.getMessage());
                return createErrorMessage("登录处理失败: " + e.getMessage());
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
    private Message handleFileOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }
        return fileService.handleRequest(message, currentUser);
    }
    /**
     * 处理用户注册
     */
    private Message handleRegister(Message message) {
        if (userService != null) {
            try {
                return userService.handleRequest(message, null);
            } catch (Exception e) {
                System.err.println("注册处理异常: " + e.getMessage());
                return createErrorMessage("注册处理失败: " + e.getMessage());
            }
        }
        return createErrorMessage("用户服务未初始化");
    }

    /**
     * 处理用户信息更新
     */
    private Message handleUserUpdate(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        if (userService != null) {
            try {
                Message response = userService.handleRequest(message, currentUser);
                // 如果更新成功且是当前用户，更新currentUser
                if (response.getCode() == Message.Code.SUCCESS &&
                        message.getData() instanceof User) {
                    User updateUser = (User) message.getData();
                    if (currentUser.getUserId().equals(updateUser.getUserId())) {
                        currentUser = updateUser;
                    }
                }
                return response;
            } catch (Exception e) {
                System.err.println("用户更新处理异常: " + e.getMessage());
                return createErrorMessage("更新处理失败: " + e.getMessage());
            }
        }
        return createErrorMessage("用户服务未初始化");
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

        if (studentService != null) {
            try {
                return studentService.handleRequest(message, currentUser);
            } catch (Exception e) {
                System.err.println("学生操作处理异常: " + e.getMessage());
                return createErrorMessage("学生操作处理失败: " + e.getMessage());
            }
        }
        return createErrorMessage("学生服务未初始化");
    }

    /**
     * 处理课程管理相关操作
     */
    private Message handleCourseOperation(Message message) {
        if (!isLoggedIn()) {
            System.err.println("课程操作失败: 用户未登录");
            return createErrorMessage("请先登录");
        }

        if (courseService == null) {
            System.err.println("课程操作失败: CourseService 未初始化");
            return createErrorMessage("课程服务未初始化");
        }

        try {
            System.out.println("转发消息到 CourseService: " + message.getType());
            Message response = courseService.handleRequest(message, currentUser);
            System.out.println("CourseService 响应: " + response.getCode());
            return response;
        } catch (Exception e) {
            System.err.println("课程操作处理异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("课程操作处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理图书馆相关操作
     */
    private Message handleLibraryOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        if (libraryService != null) {
            try {
                return libraryService.handleRequest(message, currentUser);
            } catch (Exception e) {
                System.err.println("图书馆操作处理异常: " + e.getMessage());
                return createErrorMessage("图书馆操作处理失败: " + e.getMessage());
            }
        }
        return createErrorMessage("图书馆服务未初始化");
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

    /**
     * 处理教务系统相关操作（新增）
     */
    private Message handleOnlineClassOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return OnlineClassService.handleRequest(message, currentUser);
    }

    /**
     * 处理生活服务相关操作
     */
    private Message handleLifeServiceOperation(Message message) {
        if (!isLoggedIn()) {
            return createErrorMessage("请先登录");
        }

        return LifeServiceService.handleRequest(message, currentUser);
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
            if (server != null) {
                server.removeClient(clientId);
            }

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