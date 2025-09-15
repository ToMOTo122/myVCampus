// ============= 完整修复的CourseService.java =============
package com.vcampus.server.service;

import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.CourseSelection;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseService {
    private Connection connection;

    // 构造函数
    public CourseService() {
        // 默认构造函数，使用DatabaseHelper获取连接
    }

    public CourseService(Connection connection) {
        this.connection = connection;
    }

    // 处理客户端请求的主要方法
    public Message handleRequest(Message request, User currentUser) {
        if (request == null) {
            return createErrorMessage("请求消息为空");
        }

        System.out.println("CourseService处理请求: " + request.getType());

        try {
            switch (request.getType()) {
                case COURSE_LIST:
                    return getAllCourses();

                case STUDENT_SELECTED_COURSES:
                    if (request.getData() != null) {
                        String studentId = request.getData().toString();
                        return getStudentSelectedCourses(studentId);
                    }
                    return createErrorMessage("学生ID不能为空");

                case SELECT_COURSE:
                    if (request.getData() instanceof CourseSelection) {
                        return selectCourse((CourseSelection) request.getData());
                    }
                    return createErrorMessage("选课数据格式错误");

                case DROP_COURSE:
                    if (request.getData() instanceof CourseSelection) {
                        return dropCourse((CourseSelection) request.getData());
                    }
                    return createErrorMessage("退选数据格式错误");

                case COURSE_SELECTIONS:
                    if (request.getData() != null) {
                        String courseId = request.getData().toString();
                        return getCourseSelections(courseId);
                    }
                    return createErrorMessage("课程ID不能为空");

                case TEACHER_COURSES:
                    if (request.getData() != null) {
                        String teacherId = request.getData().toString();
                        return getTeacherCourses(teacherId);
                    }
                    return createErrorMessage("教师ID不能为空");

                case USER_LIST:
                    return getAllUsers();

                case ADD_COURSE:
                    if (request.getData() instanceof Course) {
                        return addCourse((Course) request.getData());
                    }
                    return createErrorMessage("课程数据格式错误");

                case UPDATE_COURSE:
                    if (request.getData() instanceof Course) {
                        return updateCourse((Course) request.getData());
                    }
                    return createErrorMessage("课程数据格式错误");

                case DELETE_COURSE:
                    if (request.getData() != null) {
                        String courseId = request.getData().toString();
                        return deleteCourse(courseId);
                    }
                    return createErrorMessage("课程ID不能为空");

                default:
                    return createErrorMessage("不支持的操作类型: " + request.getType());
            }

        } catch (Exception e) {
            System.err.println("CourseService处理请求时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        }
    }

    // 获取指定课程的选课记录
    public Message getCourseSelections(String courseId) {
        System.out.println("CourseService.getCourseSelections 被调用，课程ID: " + courseId);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            if (courseId == null || courseId.trim().isEmpty()) {
                return createErrorMessage("课程ID不能为空");
            }

            conn = getConnection();
            String sql = "SELECT student_id, course_id, selection_time, status FROM tbl_course_selection WHERE course_id = ?";
            System.out.println("执行SQL: " + sql + ", 参数: " + courseId);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            List<CourseSelection> selections = new ArrayList<>();
            while (rs.next()) {
                CourseSelection selection = new CourseSelection();
                selection.setStudentId(rs.getString("student_id"));
                selection.setCourseId(rs.getString("course_id"));
                selection.setSelectionTime(rs.getTimestamp("selection_time"));

                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        selection.setStatus(CourseSelection.Status.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        System.out.println("未知状态: " + statusStr + ", 使用默认值SELECTED");
                        selection.setStatus(CourseSelection.Status.SELECTED);
                    }
                } else {
                    selection.setStatus(CourseSelection.Status.SELECTED);
                }
                selections.add(selection);
            }

            System.out.println("找到 " + selections.size() + " 条选课记录");
            return new Message(Message.Type.COURSE_SELECTIONS, Message.Code.SUCCESS, selections);

        } catch (SQLException e) {
            System.err.println("获取课程选课记录时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("数据库查询失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("获取课程选课记录时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // 选课方法
    public Message selectCourse(CourseSelection selection) {
        System.out.println("CourseService.selectCourse: " + selection.getStudentId() + " -> " + selection.getCourseId());

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement courseStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet checkRs = null;
        ResultSet courseRs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 检查是否已选课
            String checkSql = "SELECT status FROM tbl_course_selection WHERE student_id = ? AND course_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, selection.getStudentId());
            checkStmt.setString(2, selection.getCourseId());
            checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                String existingStatus = checkRs.getString("status");
                if (!"DROPPED".equals(existingStatus)) {
                    conn.rollback();
                    return createErrorMessage("您已经选择了这门课程");
                } else {
                    // 重新激活选课记录
                    String updateSql = "UPDATE tbl_course_selection SET status = 'SELECTED', selection_time = NOW() WHERE student_id = ? AND course_id = ?";
                    updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, selection.getStudentId());
                    updateStmt.setString(2, selection.getCourseId());

                    if (updateStmt.executeUpdate() > 0) {
                        updateCourseEnrolledCount(selection.getCourseId(), conn);
                        conn.commit();
                        System.out.println("重新选课成功");
                        return createSuccessMessage("选课成功");
                    } else {
                        conn.rollback();
                        return createErrorMessage("更新选课记录失败");
                    }
                }
            }

            // 检查课程容量
            String courseSql = "SELECT capacity FROM tbl_course WHERE course_id = ?";
            courseStmt = conn.prepareStatement(courseSql);
            courseStmt.setString(1, selection.getCourseId());
            courseRs = courseStmt.executeQuery();

            if (!courseRs.next()) {
                conn.rollback();
                return createErrorMessage("课程不存在");
            }

            int capacity = courseRs.getInt("capacity");
            int currentEnrolled = getCurrentEnrolledCount(selection.getCourseId(), conn);

            System.out.println("课程容量: " + capacity + ", 当前选课人数: " + currentEnrolled);

            if (currentEnrolled >= capacity) {
                conn.rollback();
                return createErrorMessage("课程已满员");
            }

            // 插入新选课记录
            String insertSql = "INSERT INTO tbl_course_selection (student_id, course_id, selection_time, status) VALUES (?, ?, NOW(), 'SELECTED')";
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, selection.getStudentId());
            insertStmt.setString(2, selection.getCourseId());

            if (insertStmt.executeUpdate() > 0) {
                updateCourseEnrolledCount(selection.getCourseId(), conn);
                conn.commit();
                System.out.println("选课成功");
                return createSuccessMessage("选课成功");
            } else {
                conn.rollback();
                return createErrorMessage("选课失败");
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚事务失败: " + rollbackEx.getMessage());
            }

            System.err.println("选课时发生数据库错误: " + e.getMessage());
            e.printStackTrace();

            if (e.getMessage().contains("Duplicate entry")) {
                return createErrorMessage("您已经选择了这门课程");
            }
            return createErrorMessage("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚事务失败: " + rollbackEx.getMessage());
            }
            System.err.println("选课时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("恢复自动提交失败: " + e.getMessage());
            }
            closeResources(conn, checkStmt, checkRs);
            closeStatement(courseStmt);
            closeResultSet(courseRs);
            closeStatement(updateStmt);
            closeStatement(insertStmt);
        }
    }

    // 退选课程
    public Message dropCourse(CourseSelection selection) {
        System.out.println("CourseService.dropCourse: " + selection.getStudentId() + " -> " + selection.getCourseId());

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet checkRs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT status FROM tbl_course_selection WHERE student_id = ? AND course_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, selection.getStudentId());
            checkStmt.setString(2, selection.getCourseId());
            checkRs = checkStmt.executeQuery();

            if (!checkRs.next()) {
                conn.rollback();
                return createErrorMessage("未找到选课记录");
            }

            if ("DROPPED".equals(checkRs.getString("status"))) {
                conn.rollback();
                return createErrorMessage("该课程已经退选");
            }

            String updateSql = "UPDATE tbl_course_selection SET status = 'DROPPED' WHERE student_id = ? AND course_id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, selection.getStudentId());
            updateStmt.setString(2, selection.getCourseId());

            if (updateStmt.executeUpdate() > 0) {
                updateCourseEnrolledCount(selection.getCourseId(), conn);
                conn.commit();
                System.out.println("退选成功");
                return createSuccessMessage("退选成功");
            } else {
                conn.rollback();
                return createErrorMessage("退选失败");
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚事务失败: " + rollbackEx.getMessage());
            }
            System.err.println("退选时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚事务失败: " + rollbackEx.getMessage());
            }
            System.err.println("退选时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("恢复自动提交失败: " + e.getMessage());
            }
            closeResources(conn, checkStmt, checkRs);
            closeStatement(updateStmt);
        }
    }

    // 获取学生已选课程
    public Message getStudentSelectedCourses(String studentId) {
        System.out.println("CourseService.getStudentSelectedCourses 被调用，学生ID: " + studentId);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT student_id, course_id, selection_time, status FROM tbl_course_selection WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            List<CourseSelection> selections = new ArrayList<>();
            while (rs.next()) {
                CourseSelection selection = new CourseSelection();
                selection.setStudentId(rs.getString("student_id"));
                selection.setCourseId(rs.getString("course_id"));
                selection.setSelectionTime(rs.getTimestamp("selection_time"));

                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        selection.setStatus(CourseSelection.Status.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        selection.setStatus(CourseSelection.Status.SELECTED);
                    }
                } else {
                    selection.setStatus(CourseSelection.Status.SELECTED);
                }
                selections.add(selection);
            }

            System.out.println("学生 " + studentId + " 已选课程: " + selections.size() + " 门");
            return new Message(Message.Type.STUDENT_SELECTED_COURSES, Message.Code.SUCCESS, selections);

        } catch (SQLException e) {
            System.err.println("获取学生已选课程时发生数据库错误: " + e.getMessage());
            return createErrorMessage("数据库查询失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("获取学生已选课程时发生错误: " + e.getMessage());
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // 获取所有课程
    public Message getAllCourses() {
        System.out.println("CourseService.getAllCourses 被调用");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT * FROM tbl_course WHERE is_online = 0";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Course> courses = new ArrayList<>();
            while (rs.next()) {
                Course course = createCourseFromResultSet(rs);
                courses.add(course);
            }

            System.out.println("获取到 " + courses.size() + " 门课程");
            return new Message(Message.Type.COURSE_LIST, Message.Code.SUCCESS, courses);

        } catch (SQLException e) {
            System.err.println("获取所有课程时发生数据库错误: " + e.getMessage());
            return createErrorMessage("数据库查询失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("获取所有课程时发生错误: " + e.getMessage());
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // 获取教师课程 - 关键修复
    public Message getTeacherCourses(String teacherId) {
        System.out.println("CourseService.getTeacherCourses 被调用，教师ID: " + teacherId);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            if (teacherId == null || teacherId.trim().isEmpty()) {
                return createErrorMessage("教师ID不能为空");
            }

            conn = getConnection();
            String sql = "SELECT * FROM tbl_course WHERE teacher_id = ? AND is_online = 0";
            System.out.println("执行SQL: " + sql + ", 参数: " + teacherId);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherId);
            rs = stmt.executeQuery();

            List<Course> courses = new ArrayList<>();
            while (rs.next()) {
                Course course = createCourseFromResultSet(rs);
                courses.add(course);
            }

            System.out.println("找到 " + courses.size() + " 门教师课程");
            return new Message(Message.Type.TEACHER_COURSES, Message.Code.SUCCESS, courses);

        } catch (SQLException e) {
            System.err.println("获取教师课程时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("数据库查询失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("获取教师课程时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // 获取所有用户 - 修复数据库字段映射
    public Message getAllUsers() {
        System.out.println("CourseService.getAllUsers 被调用");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT * FROM tbl_user";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));

                // 使用real_name作为主要姓名字段
                String realName = rs.getString("real_name");
                if (realName != null && !realName.trim().isEmpty()) {
                    user.setRealName(realName);
                    user.setDisplayName(realName);
                } else {
                    // 如果real_name为空，尝试使用user_id作为显示名称
                    user.setRealName(rs.getString("user_id"));
                    user.setDisplayName(rs.getString("user_id"));
                }

                // 设置其他属性，使用安全的字段获取方式
                try { user.setMajor(rs.getString("major")); } catch (SQLException e) { /* 字段不存在时忽略 */ }
                try { user.setGrade(rs.getString("grade")); } catch (SQLException e) { /* 字段不存在时忽略 */ }
                try { user.setDepartment(rs.getString("department")); } catch (SQLException e) { /* 字段不存在时忽略 */ }
                try { user.setClassName(rs.getString("class_name")); } catch (SQLException e) { /* 字段不存在时忽略 */ }
                try { user.setEmail(rs.getString("email")); } catch (SQLException e) { /* 字段不存在时忽略 */ }
                try { user.setPhone(rs.getString("phone")); } catch (SQLException e) { /* 字段不存在时忽略 */ }

                // 设置年龄
                try {
                    int age = rs.getInt("age");
                    user.setAge(age);
                } catch (SQLException e) { /* 字段不存在时忽略 */ }

                // 设置角色
                String roleStr = rs.getString("role");
                if (roleStr != null) {
                    try {
                        user.setRole(User.Role.valueOf(roleStr));
                    } catch (IllegalArgumentException e) {
                        System.out.println("未知角色: " + roleStr + ", 设置为默认学生角色");
                        user.setRole(User.Role.STUDENT); // 默认为学生
                    }
                } else {
                    user.setRole(User.Role.STUDENT); // 默认为学生
                }

                users.add(user);
                System.out.println("加载用户: ID=" + user.getUserId() +
                        ", 姓名=" + user.getRealName() +
                        ", 角色=" + user.getRole());
            }

            System.out.println("获取到 " + users.size() + " 个用户");
            return new Message(Message.Type.USER_LIST, Message.Code.SUCCESS, users);

        } catch (SQLException e) {
            System.err.println("获取用户列表时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("数据库查询失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("获取用户列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    // 添加课程
    public Message addCourse(Course course) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            String sql = "INSERT INTO tbl_course (course_id, course_name, teacher_id, teacher_name, credits, capacity, department, course_type, classroom, semester, description, schedule, enrolled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, course.getCourseId());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getTeacherId());
            stmt.setString(4, course.getTeacherName());
            stmt.setInt(5, course.getCredits());
            stmt.setInt(6, course.getCapacity());
            stmt.setString(7, course.getDepartment());
            stmt.setString(8, course.getCourseType());
            stmt.setString(9, course.getClassroom());
            stmt.setString(10, course.getSemester());
            stmt.setString(11, course.getDescription());
            stmt.setString(12, course.getSchedule());

            if (stmt.executeUpdate() > 0) {
                System.out.println("课程添加成功: " + course.getCourseId());
                return createSuccessMessage("课程添加成功");
            } else {
                return createErrorMessage("课程添加失败");
            }

        } catch (SQLException e) {
            System.err.println("添加课程时发生数据库错误: " + e.getMessage());
            return createErrorMessage("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("添加课程时发生错误: " + e.getMessage());
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt);
        }
    }

    // 更新课程
    public Message updateCourse(Course course) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            String sql = "UPDATE tbl_course SET course_name=?, teacher_id=?, teacher_name=?, credits=?, capacity=?, department=?, course_type=?, classroom=?, semester=?, description=?, schedule=? WHERE course_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, course.getCourseName());
            stmt.setString(2, course.getTeacherId());
            stmt.setString(3, course.getTeacherName());
            stmt.setInt(4, course.getCredits());
            stmt.setInt(5, course.getCapacity());
            stmt.setString(6, course.getDepartment());
            stmt.setString(7, course.getCourseType());
            stmt.setString(8, course.getClassroom());
            stmt.setString(9, course.getSemester());
            stmt.setString(10, course.getDescription());
            stmt.setString(11, course.getSchedule());
            stmt.setString(12, course.getCourseId());

            if (stmt.executeUpdate() > 0) {
                System.out.println("课程更新成功: " + course.getCourseId());
                return createSuccessMessage("课程更新成功");
            } else {
                return createErrorMessage("课程更新失败，课程不存在");
            }

        } catch (SQLException e) {
            System.err.println("更新课程时发生数据库错误: " + e.getMessage());
            return createErrorMessage("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("更新课程时发生错误: " + e.getMessage());
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt);
        }
    }

    // 删除课程
    public Message deleteCourse(String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            String sql = "DELETE FROM tbl_course WHERE course_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);

            if (stmt.executeUpdate() > 0) {
                System.out.println("课程删除成功: " + courseId);
                return createSuccessMessage("课程删除成功");
            } else {
                return createErrorMessage("课程删除失败，课程不存在");
            }

        } catch (SQLException e) {
            System.err.println("删除课程时发生数据库错误: " + e.getMessage());
            return createErrorMessage("数据库错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("删除课程时发生错误: " + e.getMessage());
            return createErrorMessage("系统错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt);
        }
    }

    // 辅助方法 - 从ResultSet创建Course对象
    private Course createCourseFromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getString("course_id"));
        course.setCourseName(rs.getString("course_name"));
        course.setTeacherName(rs.getString("teacher_name"));
        course.setTeacherId(rs.getString("teacher_id"));
        course.setCredits(rs.getInt("credits"));
        course.setCapacity(rs.getInt("capacity"));
        course.setEnrolled(rs.getInt("enrolled"));
        course.setDepartment(rs.getString("department"));
        course.setCourseType(rs.getString("course_type"));
        course.setClassroom(rs.getString("classroom"));
        course.setSemester(rs.getString("semester"));
        course.setDescription(rs.getString("description"));
        course.setSchedule(rs.getString("schedule"));
        return course;
    }

    // 获取当前有效选课人数
    private int getCurrentEnrolledCount(String courseId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_course_selection WHERE course_id = ? AND status != 'DROPPED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // 更新课程选课人数
    private void updateCourseEnrolledCount(String courseId, Connection conn) throws SQLException {
        int currentCount = getCurrentEnrolledCount(courseId, conn);
        String updateSql = "UPDATE tbl_course SET enrolled = ? WHERE course_id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, currentCount);
            updateStmt.setString(2, courseId);
            int result = updateStmt.executeUpdate();
            System.out.println("更新课程 " + courseId + " 的选课人数为: " + currentCount + ", 影响行数: " + result);
        }
    }

    // 获取数据库连接
    private Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        return DatabaseHelper.getConnection();
    }

    // 资源关闭方法
    private void closeResources(Connection conn, PreparedStatement stmt) {
        closeResources(conn, stmt, null);
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (connection == null) {
            // 只有在没有注入连接时才关闭连接
            DatabaseHelper.closeResources(conn, stmt, rs);
        } else {
            // 如果使用注入的连接，只关闭stmt和rs
            closeResultSet(rs);
            closeStatement(stmt);
        }
    }

    private void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("关闭PreparedStatement失败: " + e.getMessage());
            }
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("关闭ResultSet失败: " + e.getMessage());
            }
        }
    }

    // 消息创建方法
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

    // 验证课程数据的方法
    private boolean isValidCourse(Course course) {
        return course != null &&
                course.getCourseId() != null && !course.getCourseId().trim().isEmpty() &&
                course.getCourseName() != null && !course.getCourseName().trim().isEmpty() &&
                course.getTeacherId() != null && !course.getTeacherId().trim().isEmpty() &&
                course.getCredits() > 0 &&
                course.getCapacity() > 0;
    }

    // 验证选课数据的方法
    private boolean isValidCourseSelection(CourseSelection selection) {
        return selection != null &&
                selection.getStudentId() != null && !selection.getStudentId().trim().isEmpty() &&
                selection.getCourseId() != null && !selection.getCourseId().trim().isEmpty();
    }

    /**
     * 检查教师是否有权限管理指定课程
     */
    private boolean hasTeacherPermission(String teacherId, String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT 1 FROM tbl_course WHERE course_id = ? AND teacher_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            stmt.setString(2, teacherId);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("检查教师权限时发生错误: " + e.getMessage());
            return false;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取课程的基本信息
     */
    public Course getCourseInfo(String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT * FROM tbl_course WHERE course_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return createCourseFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("获取课程信息时发生错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * 批量更新课程选课人数
     */
    public void batchUpdateEnrolledCounts() {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 获取所有课程ID
            String selectSql = "SELECT course_id FROM tbl_course";
            selectStmt = conn.prepareStatement(selectSql);
            rs = selectStmt.executeQuery();

            List<String> courseIds = new ArrayList<>();
            while (rs.next()) {
                courseIds.add(rs.getString("course_id"));
            }

            // 更新每门课程的选课人数
            String updateSql = "UPDATE tbl_course SET enrolled = (SELECT COUNT(*) FROM tbl_course_selection WHERE course_id = ? AND status != 'DROPPED') WHERE course_id = ?";
            updateStmt = conn.prepareStatement(updateSql);

            for (String courseId : courseIds) {
                updateStmt.setString(1, courseId);
                updateStmt.setString(2, courseId);
                updateStmt.addBatch();
            }

            updateStmt.executeBatch();
            conn.commit();

            System.out.println("批量更新 " + courseIds.size() + " 门课程的选课人数完成");

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("回滚批量更新失败: " + rollbackEx.getMessage());
            }
            System.err.println("批量更新选课人数时发生错误: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("恢复自动提交失败: " + e.getMessage());
            }
            closeResources(conn, selectStmt, rs);
            closeStatement(updateStmt);
        }
    }

    /**
     * 获取课程统计信息
     */
    public Message getCourseStatistics() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT " +
                    "COUNT(*) as total_courses, " +
                    "SUM(enrolled) as total_students, " +
                    "AVG(enrolled * 1.0 / capacity) as avg_enrollment_rate, " +
                    "COUNT(CASE WHEN enrolled >= capacity THEN 1 END) as full_courses " +
                    "FROM tbl_course";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("totalCourses", rs.getInt("total_courses"));
                stats.put("totalStudents", rs.getInt("total_students"));
                stats.put("averageEnrollmentRate", rs.getDouble("avg_enrollment_rate"));
                stats.put("fullCourses", rs.getInt("full_courses"));

                return createSuccessMessage(stats);
            }

        } catch (SQLException e) {
            System.err.println("获取课程统计信息时发生错误: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }

        return createErrorMessage("获取统计信息失败");
    }
}