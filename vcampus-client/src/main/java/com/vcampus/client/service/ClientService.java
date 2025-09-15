package com.vcampus.client.service;

import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.CourseSelection;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

import com.vcampus.common.entity.*;
import com.vcampus.common.util.CommonUtils;

import java.io.*;

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

            // *** 关键添加：连接成功后立即设置到ServiceManager ***
            ServiceManager.getInstance().setClientService(this);

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
            return Message.error(Message.Code.ERROR, "请求失败: " + e.getMessage());
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
    /**
     * 获取学生已选课程
     */
    public java.util.List<CourseSelection> getStudentSelectedCourses(String studentId) {
        try {
            Message request = new Message(Message.Type.STUDENT_SELECTED_COURSES, studentId);
            Message response = sendAndReceive(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<CourseSelection>) response.getData();
            } else {
                System.err.println("获取已选课程失败: " + response.getData());
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取已选课程请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 选择课程
     */
    public boolean selectCourse(CourseSelection selection) {
        try {
            Message request = new Message(Message.Type.SELECT_COURSE, selection);
            Message response = sendAndReceive(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("选课请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 退选课程
     */
    public boolean dropCourse(CourseSelection selection) {
        try {
            Message request = new Message(Message.Type.DROP_COURSE, selection);
            Message response = sendAndReceive(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("退选请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取教师课程
     */
    public java.util.List<Course> getTeacherCourses(String teacherId) {
        try {
            Message request = new Message(Message.Type.TEACHER_COURSES, teacherId);
            Message response = sendAndReceive(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<Course>) response.getData();
            } else {
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取教师课程请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取课程选课学生
     */
    public java.util.List<com.vcampus.common.entity.User> getCourseStudents(String courseId) {
        try {
            Message request = new Message(Message.Type.COURSE_STUDENTS, courseId);
            Message response = sendAndReceive(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<com.vcampus.common.entity.User>) response.getData();
            } else {
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取选课学生请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }


    /**
     * 获取课程学生列表
     */
    public List<Student> getCourseStudents1(String courseId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_COURSE_STUDENTS, courseId);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Student.class);
    }



    /**
     * 获取所有课程
     */
    public java.util.List<Course> getAllCourses() {
        try {
            Message request = new Message(Message.Type.ALL_COURSES, null);
            Message response = sendAndReceive(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (java.util.List<Course>) response.getData();
            } else {
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取所有课程请求失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 添加课程
     */
    public boolean addCourse(Course course) {
        try {
            Message request = new Message(Message.Type.ADD_COURSE, course);
            Message response = sendAndReceive(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("添加课程请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新课程
     */
    public boolean updateCourse(Course course) {
        try {
            Message request = new Message(Message.Type.UPDATE_COURSE, course);
            Message response = sendAndReceive(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("更新课程请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除课程
     */
    public boolean deleteCourse(String courseId) {
        try {
            Message request = new Message(Message.Type.DELETE_COURSE, courseId);
            Message response = sendAndReceive(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("删除课程请求失败: " + e.getMessage());
            return false;
        }
    }
    //在线课堂
    /**
     * 获取教师待办事项
     */
    public List<String> getTeacherTodos(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_TEACHER_TODOS, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), String.class);
    }

    /**
     * 获取某个月份有提醒的日期列表
     */
    public List<Integer> getMonthReminders(int year, int month) {
        int[] params = {year, month};
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_MONTH_REMINDERS, params);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Integer.class);
    }

    /**
     * 获取某一天的提醒
     */
    public List<Reminder> getDayReminders(int year, int month, int day) {
        int[] params = {year, month, day};
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_DAY_REMINDERS, params);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Reminder.class);
    }
    /**
     * 获取学生的课程列表
     */
    public List<Course> getStudentCourses(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_STUDENT_COURSES, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Course.class);
    }

    /**
     * 搜索课程
     */
    public List<Course> searchCourses(String keyword, String filter,  User user) {
        String[] params = {keyword, filter};
        Message request = new Message(Message.Type.ONLINE_CLASS_SEARCH_COURSES, params);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Course.class);
    }

    /**
     * 获取课程统计信息
     */
    public CourseStats getCourseStats(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_COURSE_STATS, user);
        Message response = sendRequest(request);
        return convertToObject(response.getData(), CourseStats.class);
    }
    /**
     * 获取课程资料列表
     */
    public List<CourseMaterial> getCourseMaterials(String courseId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_COURSE_MATERIALS, courseId);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), CourseMaterial.class);
    }

    /**
     * 获取课程回放列表
     */
    public List<CoursePlayback> getCoursePlaybacks(String courseId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_COURSE_PLAYBACKS, courseId);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), CoursePlayback.class);
    }

    /**
     * 获取学生作业列表
     */
    public List<Assignment> getStudentAssignments(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Assignment.class);
    }

    /**
     * 搜索作业
     */
    public List<Assignment> searchAssignments(String keyword, String filter, User user) {
        Object[] params = {keyword, filter, user};
        Message request = new Message(Message.Type.ONLINE_CLASS_SEARCH_ASSIGNMENTS, params);
        Message response = sendRequest(request);
        if (response.getCode() == Message.Code.SUCCESS) {
            return CommonUtils.convertToGenericList(response.getData(), Assignment.class);
        } else {
            System.err.println("搜索作业失败: " + response.getData());
            return new ArrayList<>();
        }
    }

    /**
     * 获取作业统计信息
     */
    public AssignmentStats getAssignmentStats(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_ASSIGNMENT_STATS, user);
        Message response = sendRequest(request);
        return convertToObject(response.getData(), AssignmentStats.class);
    }

    /**
     * 提交作业
     */
    public boolean submitAssignment(String assignmentName, String filePath, User user) {
        String[] params = {assignmentName, filePath};
        Message request = new Message(Message.Type.ONLINE_CLASS_SUBMIT_ASSIGNMENT, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取最近活动
     */
    public List<String> getRecentActivities(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_RECENT_ACTIVITIES, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), String.class);
    }

    /**
     * 添加提醒
     */
    public boolean addReminder(String content, Date date, User user) {
        Object[] params = {content, date, user};
        Message request = new Message(Message.Type.ONLINE_CLASS_ADD_REMINDER, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取讨论区内容
     */
    public List<String> getDiscussions(String courseName) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_DISCUSSIONS, courseName);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), String.class);
    }

    /**
     * 发表讨论
     */
    public boolean postDiscussion(String courseName, String content, User user) {
        String[] params = {courseName, content};
        Message request = new Message(Message.Type.ONLINE_CLASS_POST_DISCUSSION, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取教师课程列表
     */
    public List<Course> getTeacherCourses(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_TEACHER_COURSES, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Course.class);
    }

    /**
     * 获取班级列表
     */
    public List<String> getClassNames() {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_CLASS_NAMES, null);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), String.class);
    }

    /**
     * 添加课程
     */
    public boolean addCourse(Course course, User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_ADD_COURSE, course);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 添加课程回放
     */
    public boolean addPlayback(String courseId, CoursePlayback playback, User user) {
        Object[] params = {courseId, playback, user};
        Message request = new Message(Message.Type.ONLINE_CLASS_ADD_PLAYBACK, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 添加课程资料
     */
    public boolean addMaterial(String courseId, CourseMaterial material, User user) {
        Object[] params = {courseId, material, user};
        Message request = new Message(Message.Type.ONLINE_CLASS_ADD_MATERIAL, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取教师作业列表
     */
    public List<Assignment> getTeacherAssignments(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_TEACHER_ASSIGNMENTS, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Assignment.class);
    }

    /**
     * 删除课程资料
     */
    public boolean deleteMaterial(int materialId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_DELETE_MATERIAL, materialId);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 删除课程回放
     */
    public boolean deletePlayback(int playbackId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_DELETE_PLAYBACK, playbackId);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取课程讨论（包括回复）
     */
    public List<Discussion> getCourseDiscussions(String courseId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_COURSE_DISCUSSIONS, courseId);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Discussion.class);
    }

    /**
     * 回复讨论
     */
    public boolean replyToDiscussion(String courseId, int parentId, String content) {
        String[] params = {courseId, String.valueOf(parentId), content};
        Message request = new Message(Message.Type.ONLINE_CLASS_REPLY_TO_DISCUSSION, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 发布作业
     */
    public boolean publishAssignment(Assignment assignment) {
        Message request = new Message(Message.Type.ONLINE_CLASS_PUBLISH_ASSIGNMENT, assignment);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 批改作业
     */
    public boolean gradeAssignment(String studentId, int assignmentId, int score, String feedback) {
        Object[] params = {studentId, assignmentId, score, feedback};
        Message request = new Message(Message.Type.ONLINE_CLASS_GRADE_ASSIGNMENT, params);
        Message response = sendRequest(request);
        return response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 获取学生作业列表（教师视角）
     */
    public List<StudentAssignment> getStudentAssignmentsForTeacher(int assignmentId) {
        try {
            Message request = new Message(Message.Type.ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS_FOR_TEACHER, assignmentId);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), StudentAssignment.class);
            } else {
                System.err.println("获取学生作业列表失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取学生作业列表请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    /**
     * 获取教师所教课程列表（用于发布作业时选择课程）
     */
    public List<Course> getTeacherCoursesForAssignment(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_TEACHER_COURSES_FOR_ASSIGNMENT, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Course.class);
    }

    /**
     * 获取作业详情（包括提交情况）
     */
    public Assignment getAssignmentDetails(int assignmentId) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_ASSIGNMENT_DETAILS, assignmentId);
        Message response = sendRequest(request);
        return (Assignment) response.getData();
    }

    /**
     * 搜索教师作业
     */
    public List<Assignment> searchTeacherAssignments(String keyword, String courseFilter, String statusFilter, User user) {
        Object[] params = {keyword, courseFilter, statusFilter};
        Message request = new Message(Message.Type.ONLINE_CLASS_SEARCH_TEACHER_ASSIGNMENTS, params);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Assignment.class);
    }

    /**
     * 获取教师所教课程名称列表
     */
    public List<String> getTeacherCourseNames(User user) {
        Message request = new Message(Message.Type.ONLINE_CLASS_GET_TEACHER_COURSE_NAMES, user);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), String.class);
    }

    /**
     * 搜索教师课程
     */
    public List<Course> searchTeacherCourses(String keyword, User user) {
        Object[] params = {keyword, user};
        Message request = new Message(Message.Type.ONLINE_CLASS_SEARCH_TEACHER_COURSES, params);
        Message response = sendRequest(request);
        return CommonUtils.convertToGenericList(response.getData(), Course.class);
    }

    /**
     * 获取作业批改详情
     */
    public Map<String, String> getAssignmentFeedback(String assignmentName, User user) {
        try {
            Message request = new Message(Message.Type.ONLINE_CLASS_GET_ASSIGNMENT_FEEDBACK,
                    new Object[]{assignmentName, user});
            Message response = sendRequest(request);

            if (response.getType() == Message.Type.SUCCESS) {
                return (Map<String, String>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 将 Object 转换为指定类型的对象
     */
    private <T> T convertToObject(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        } else if (obj instanceof Map) {
            // 如果是 Map，尝试使用 fromMap 方法转换
            try {
                Method fromMap = clazz.getMethod("fromMap", Map.class);
                return (T) fromMap.invoke(null, obj);
            } catch (Exception e) {
                System.err.println("转换失败: " + e.getMessage());
                return null;
            }
        }
        return null;
    }


    //lifeservice

    /**
     * 获取一卡通信息
     */
    public CardInfo getCardInfo(String userId) {
        try {
            Message request = new Message(Message.Type.CARD_GET_INFO, userId);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return (CardInfo) response.getData();
            } else {
                System.err.println("获取一卡通信息失败: " + response.getData());
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取一卡通信息请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取消费记录
     */
    public List<CardConsumption> getConsumptionRecords(String cardId, int limit) {
        try {
            Object[] params = {cardId, limit};
            Message request = new Message(Message.Type.CARD_GET_CONSUMPTION, params);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), CardConsumption.class);
            } else {
                System.err.println("获取消费记录失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取消费记录请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取充值记录
     */
    public List<CardRecharge> getRechargeRecords(String cardId, int limit) {
        try {
            Object[] params = {cardId, limit};
            Message request = new Message(Message.Type.CARD_GET_RECHARGE, params);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), CardRecharge.class);
            } else {
                System.err.println("获取充值记录失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取充值记录请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 一卡通充值
     */
    public boolean rechargeCard(String cardId, double amount, String method) {
        try {
            Object[] params = {cardId, amount, method};
            Message request = new Message(Message.Type.CARD_RECHARGE, params);
            Message response = sendRequest(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("一卡通充值请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 一卡通挂失
     */
    public boolean reportCardLoss(String cardId) {
        try {
            Message request = new Message(Message.Type.CARD_REPORT_LOSS, cardId);
            Message response = sendRequest(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("一卡通挂失请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 一卡通解挂
     */
    public boolean unfreezeCard(String cardId) {
        try {
            Message request = new Message(Message.Type.CARD_UNFREEZE, cardId);
            Message response = sendRequest(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("一卡通解挂请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取生活缴费账单
     */
    public List<LifePaymentBill> getPaymentBills(String status) {
        try {
            Message request = new Message(Message.Type.LIFE_PAYMENT_GET_BILLS, status);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), LifePaymentBill.class);
            } else {
                System.err.println("获取缴费账单失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取缴费账单请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 支付生活缴费账单
     */
    public boolean payBill(int billId, String payMethod) {
        try {
            Object[] params = {billId, payMethod};
            Message request = new Message(Message.Type.LIFE_PAYMENT_PAY, params);
            Message response = sendRequest(request);

            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("支付账单请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取缴费记录
     */
    public List<LifePaymentRecord> getPaymentRecords(int limit) {
        try {
            Message request = new Message(Message.Type.LIFE_PAYMENT_GET_RECORDS, limit);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), LifePaymentRecord.class);
            } else {
                System.err.println("获取缴费记录失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取缴费记录请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 添加一卡通
     */
    public boolean addCard(String userId, String cardId) {
        try {
            Object[] params = {userId, cardId};
            Message request = new Message(Message.Type.CARD_ADD, params);
            Message response = sendRequest(request);
            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("添加一卡通请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有报修记录
     */
    public List<RepairRecord> getAllRepairRecords(String statusFilter) {
        try {
            Message request = new Message(Message.Type.REPAIR_GET_ALL, statusFilter);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), RepairRecord.class);
            } else {
                System.err.println("获取报修记录失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取报修记录请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 分配维修人员
     */
    public boolean assignRepairHandler(int repairId, String handler) {
        try {
            Object[] params = {repairId, handler};
            Message request = new Message(Message.Type.REPAIR_ASSIGN_HANDLER, params);
            Message response = sendRequest(request);
            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("分配维修人员请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 添加缴费账单
     */
    public boolean addPaymentBill(String userId, String billType, double amount, Date dueDate) {
        try {
            Object[] params = {userId, billType, amount, dueDate};
            Message request = new Message(Message.Type.LIFE_PAYMENT_ADD_BILL, params);
            Message response = sendRequest(request);
            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("添加缴费账单请求失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 搜索用户
     */
    public List<User> searchUsers(String keyword) {
        try {
            Message request = new Message(Message.Type.USER_SEARCH, keyword);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), User.class);
            } else {
                System.err.println("搜索用户失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("搜索用户请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有一卡通信息
     */
    public List<CardInfo> getAllCards() {
        try {
            Message request = new Message(Message.Type.CARD_GET_ALL, null);
            Message response = sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                return CommonUtils.convertToGenericList(response.getData(), CardInfo.class);
            } else {
                System.err.println("获取一卡通列表失败: " + response.getData());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("获取一卡通列表请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 完成报修
     */
    public boolean completeRepair(int repairId) {
        try {
            Message request = new Message(Message.Type.REPAIR_COMPLETE, repairId);
            Message response = sendRequest(request);
            return response.getCode() == Message.Code.SUCCESS;
        } catch (Exception e) {
            System.err.println("完成报修请求失败: " + e.getMessage());
            return false;
        }
    }

//yhr9.14 23:05添加
    // --- 新增的二手市场模块方法 ---
    /**
     * 获取所有二手商品列表
     */
    public Message getAllItems() {
        Message request = new Message(Message.Type.SECOND_HAND_LIST, null);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据关键字搜索二手商品
     */
    public Message searchItems(String keyword) {
        Message request = new Message(Message.Type.SECOND_HAND_SEARCH, keyword);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 发布一个新商品
     */
    public Message postNewItem(SecondHandItem item) {
        Message request = new Message(Message.Type.SECOND_HAND_POST, item);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 将商品添加到“想要”列表
     */
    public Message addWantedItem(int itemId) {
        Message request = new Message(Message.Type.SECOND_HAND_WANT, itemId);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("添加“想要”失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户发布的商品列表
     */
    public Message getMyPostedItems() {
        Message request = new Message(Message.Type.SECOND_HAND_MY_POSTS, null);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("获取我的发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户“想要”的商品列表
     */
    public Message getMyWantedItems() {
        Message request = new Message(Message.Type.SECOND_HAND_MY_WANTS, null);
        try {
            return sendAndReceive(request);
        } catch (IOException | ClassNotFoundException e) {
            return Message.error("获取我的想要失败: " + e.getMessage());
        }
    }
    //添加完毕


}