// ============= 修复后的服务器端消息处理器 =============
package com.vcampus.server.handler;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.CourseSelection;
import com.vcampus.server.service.CourseService;
import com.vcampus.server.service.UserService;

public class MessageHandler {

    private CourseService courseService;
    private UserService userService;

    public MessageHandler(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    public Message handleMessage(Message request, User currentUser) {
        try {
            System.out.println("处理消息类型: " + request.getType() + ", 当前用户: " +
                    (currentUser != null ? currentUser.getDisplayName() : "null"));

            switch (request.getType()) {
                // 课程管理相关
                case COURSE_LIST:
                    System.out.println("转发消息到 CourseService: COURSE_LIST");
                    return courseService.getAllCourses();

                case ADD_COURSE:
                    System.out.println("转发消息到 CourseService: ADD_COURSE");
                    return courseService.addCourse((Course) request.getData());

                case UPDATE_COURSE:
                    System.out.println("转发消息到 CourseService: UPDATE_COURSE");
                    return courseService.updateCourse((Course) request.getData());

                case DELETE_COURSE:
                    System.out.println("转发消息到 CourseService: DELETE_COURSE");
                    return courseService.deleteCourse((String) request.getData());

                // 选课相关 - 关键修复区域
                case SELECT_COURSE:
                    System.out.println("转发消息到 CourseService: SELECT_COURSE");
                    Message selectResult = courseService.selectCourse((CourseSelection) request.getData());
                    System.out.println("CourseService 响应: " + selectResult.getCode());
                    if (selectResult.getCode() == Message.Code.ERROR) {
                        System.out.println("选课失败: " + selectResult.getData());
                    }
                    return selectResult;

                case DROP_COURSE:
                    System.out.println("转发消息到 CourseService: DROP_COURSE");
                    Message dropResult = courseService.dropCourse((CourseSelection) request.getData());
                    System.out.println("CourseService 响应: " + dropResult.getCode());
                    if (dropResult.getCode() == Message.Code.ERROR) {
                        System.out.println("退选失败: " + dropResult.getData());
                    }
                    return dropResult;

                case STUDENT_SELECTED_COURSES:
                    System.out.println("转发消息到 CourseService: STUDENT_SELECTED_COURSES");
                    return courseService.getStudentSelectedCourses((String) request.getData());

                case TEACHER_COURSES:
                    System.out.println("转发消息到 CourseService: TEACHER_COURSES");
                    return courseService.getTeacherCourses((String) request.getData());

                // 重要：添加 COURSE_SELECTIONS 处理
                case COURSE_SELECTIONS:
                    System.out.println("转发消息到 CourseService: COURSE_SELECTIONS");
                    Message courseSelectionsResult = courseService.getCourseSelections((String) request.getData());
                    System.out.println("CourseService 响应: " + courseSelectionsResult.getCode());
                    if (courseSelectionsResult.getCode() == Message.Code.ERROR) {
                        System.out.println("获取选课记录失败: " + courseSelectionsResult.getData());
                    }
                    return courseSelectionsResult;

                // 用户相关
                case USER_LIST:
                    System.out.println("转发消息到 UserService: USER_LIST");
                    if (request.getData() instanceof User.Role) {
                        User.Role role = (User.Role) request.getData();
                        return (Message) userService.getUsersByRole(role);
                    } else {
                        return (Message) userService.getAllUsers();
                    }

                    // 系统相关
                case PING:
                    System.out.println("处理心跳检测");
                    return new Message(Message.Type.PING, Message.Code.SUCCESS, "PONG");

                default:
                    System.out.println("未处理的消息类型: " + request.getType());
                    return new Message(request.getType(), Message.Code.ERROR, "不支持的操作类型: " + request.getType());
            }

        } catch (Exception e) {
            System.err.println("处理消息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return new Message(request.getType(), Message.Code.ERROR, "服务器内部错误: " + e.getMessage());
        }
    }
}