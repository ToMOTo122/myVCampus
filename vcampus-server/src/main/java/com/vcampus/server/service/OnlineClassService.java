package com.vcampus.server.service;

import com.vcampus.common.entity.*;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnlineClassService {

    public static Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();
            Object data = message.getData();
            OnlineClassService service = new OnlineClassService();

            switch (type) {
                case ONLINE_CLASS_GET_TEACHER_TODOS:
                    List<String> todos = service.getTeacherTodos(currentUser);
                    return Message.success(todos);

                case ONLINE_CLASS_GET_TODAY_REMINDERS:
                    List<Reminder> todayReminders = service.getTodayReminders(currentUser);
                    return Message.success(todayReminders);

                case ONLINE_CLASS_GET_MONTH_REMINDERS:
                    int[] monthParams = (int[]) data;
                    List<Integer> monthReminders = service.getMonthReminders(
                            monthParams[0], monthParams[1], currentUser);
                    return Message.success(monthReminders);

                case ONLINE_CLASS_GET_DAY_REMINDERS:
                    int[] dayParams = (int[]) data;
                    List<Reminder> dayReminders = service.getDayReminders(
                            dayParams[0], dayParams[1], dayParams[2], currentUser);
                    return Message.success(dayReminders);

                case ONLINE_CLASS_GET_STUDENT_COURSES:
                    List<Course> studentCourses = service.getStudentCourses(currentUser);
                    return Message.success(studentCourses);

                case ONLINE_CLASS_SEARCH_COURSES:
                    String[] searchParams = (String[]) data;
                    List<Course> searchedCourses = service.searchCourses(
                            searchParams[0], searchParams[1], currentUser);
                    return Message.success(searchedCourses);

                case ONLINE_CLASS_GET_COURSE_STATS:
                    CourseStats courseStats = service.getCourseStats(currentUser);
                    return Message.success(courseStats);

                case ONLINE_CLASS_GET_COURSE_MATERIALS:
                    List<CourseMaterial> materials = service.getCourseMaterials((String) data);
                    return Message.success(materials);

                case ONLINE_CLASS_GET_COURSE_PLAYBACKS:
                    List<CoursePlayback> playbacks = service.getCoursePlaybacks((String) data);
                    return Message.success(playbacks);

                case ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS:
                    List<Assignment> studentAssignments = service.getStudentAssignments1(currentUser);
                    return Message.success(studentAssignments);

                case ONLINE_CLASS_SEARCH_ASSIGNMENTS:
                    Object[] assignmentParams = (Object[]) data;
                    String keyword = (String) assignmentParams[0];
                    String filter = (String) assignmentParams[1];
                    User user = (User) assignmentParams[2];
                    List<Assignment> searchedAssignments = service.searchAssignments(
                            keyword, filter, user);
                    return Message.success(searchedAssignments);

                case ONLINE_CLASS_GET_ASSIGNMENT_STATS:
                    AssignmentStats assignmentStats = service.getAssignmentStats(currentUser);
                    return Message.success(assignmentStats);

                case ONLINE_CLASS_SUBMIT_ASSIGNMENT:
                    String[] submitParams = (String[]) data;
                    boolean submitResult = service.submitAssignment(
                            submitParams[0], submitParams[1], currentUser);
                    return submitResult ? Message.success("提交成功") : Message.error("提交失败");

                case ONLINE_CLASS_GET_RECENT_ACTIVITIES:
                    List<String> activities = service.getRecentActivities(currentUser);
                    return Message.success(activities);

                case ONLINE_CLASS_ADD_REMINDER:
                    Object[] reminderParams = (Object[]) data;
                    boolean addReminderResult = service.addReminder(
                            (String) reminderParams[0], (Date) reminderParams[1], currentUser);
                    return addReminderResult ? Message.success("添加成功") : Message.error("添加失败");

                case ONLINE_CLASS_GET_DISCUSSIONS:
                    List<String> discussions = service.getDiscussions((String) data);
                    return Message.success(discussions);

                case ONLINE_CLASS_POST_DISCUSSION:
                    String[] discussionParams = (String[]) data;
                    boolean postResult = service.postDiscussion(
                            discussionParams[0], discussionParams[1], currentUser);
                    return postResult ? Message.success("发表成功") : Message.error("发表失败");

                case ONLINE_CLASS_GET_TEACHER_COURSES:
                    List<Course> teacherCourses = service.getTeacherCourses(currentUser);
                    return Message.success(teacherCourses);

                case ONLINE_CLASS_GET_TEACHER_COURSE_STATS:
                    CourseStats teacherCourseStats = service.getTeacherCourseStats(currentUser);
                    return Message.success(teacherCourseStats);

                case ONLINE_CLASS_GET_CLASS_NAMES:
                    List<String> classNames = service.getClassNames();
                    return Message.success(classNames);

                case ONLINE_CLASS_ADD_COURSE:
                    boolean addCourseResult = service.addCourse((Course) data, currentUser);
                    return addCourseResult ? Message.success("添加成功") : Message.error("添加失败");

                case ONLINE_CLASS_ADD_PLAYBACK:
                    Object[] playbackParams = (Object[]) data;
                    boolean addPlaybackResult = service.addPlayback(
                            (String) playbackParams[0], (CoursePlayback) playbackParams[1], currentUser);
                    return addPlaybackResult ? Message.success("添加成功") : Message.error("添加失败");

                case ONLINE_CLASS_ADD_MATERIAL:
                    Object[] materialParams = (Object[]) data;
                    boolean addMaterialResult = service.addMaterial(
                            (String) materialParams[0], (CourseMaterial) materialParams[1], currentUser);
                    return addMaterialResult ? Message.success("添加成功") : Message.error("添加失败");

                case ONLINE_CLASS_GET_TEACHER_ASSIGNMENTS:
                    List<Assignment> teacherAssignments = service.getTeacherAssignments(currentUser);
                    return Message.success(teacherAssignments);

                case ONLINE_CLASS_DELETE_MATERIAL:
                    boolean deleteMaterialResult = service.deleteMaterial((Integer) data, currentUser);
                    return deleteMaterialResult ? Message.success("删除成功") : Message.error("删除失败");

                case ONLINE_CLASS_DELETE_PLAYBACK:
                    boolean deletePlaybackResult = service.deletePlayback((Integer) data, currentUser);
                    return deletePlaybackResult ? Message.success("删除成功") : Message.error("删除失败");

                case ONLINE_CLASS_GET_COURSE_DISCUSSIONS:
                    List<Discussion> courseDiscussions = service.getCourseDiscussions((String) data);
                    return Message.success(courseDiscussions);

                case ONLINE_CLASS_REPLY_TO_DISCUSSION:
                    String[] replyParams = (String[]) data;
                    boolean replyResult = service.replyToDiscussion(
                            replyParams[0], replyParams[1], replyParams[2], currentUser);
                    return replyResult ? Message.success("回复成功") : Message.error("回复失败");

                case ONLINE_CLASS_PUBLISH_ASSIGNMENT:
                    boolean publishResult = service.publishAssignment((Assignment) data, currentUser);
                    return publishResult ? Message.success("发布成功") : Message.error("发布失败");

                case ONLINE_CLASS_GRADE_ASSIGNMENT:
                    Object[] gradeParams = (Object[]) data;
                    boolean gradeResult = service.gradeAssignment(
                            (String) gradeParams[0], (Integer) gradeParams[1],
                            (Integer) gradeParams[2], (String) gradeParams[3], currentUser);
                    return gradeResult ? Message.success("批改成功") : Message.error("批改失败");

                case ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS_FOR_TEACHER:
                    List<StudentAssignment> studentAssignmentsForTeacher =
                            service.getStudentAssignments((Integer) data, currentUser);
                    return Message.success(studentAssignmentsForTeacher);

                case ONLINE_CLASS_GET_COURSE_STUDENTS:
                    List<Student> courseStudents = service.getCourseStudents((String) data, currentUser);
                    return Message.success(courseStudents);

                case ONLINE_CLASS_GET_TEACHER_COURSES_FOR_ASSIGNMENT:
                    List<Course> teacherCoursesForAssignment = service.getTeacherCoursesForAssignment(currentUser);
                    return Message.success(teacherCoursesForAssignment);

                case ONLINE_CLASS_GET_ASSIGNMENT_DETAILS:
                    Assignment assignmentDetails = service.getAssignmentDetails((Integer) data, currentUser);
                    return Message.success(assignmentDetails);

                case ONLINE_CLASS_SEARCH_TEACHER_ASSIGNMENTS:
                    String[] teacherAssignmentParams = (String[]) data;
                    List<Assignment> searchedTeacherAssignments = service.searchTeacherAssignments(
                            teacherAssignmentParams[0], teacherAssignmentParams[1],
                            teacherAssignmentParams[2], currentUser);
                    return Message.success(searchedTeacherAssignments);

                case ONLINE_CLASS_GET_TEACHER_COURSE_NAMES:
                    List<String> teacherCourseNames = service.getTeacherCourseNames(currentUser);
                    return Message.success(teacherCourseNames);

                case ONLINE_CLASS_SEARCH_TEACHER_COURSES:
                    Object[] teacherCourseParams = (Object[]) data;
                    String keyword1 = (String) teacherCourseParams[0];
                    List<Course> searchedTeacherCourses = service.searchTeacherCourses(keyword1, currentUser);
                    return Message.success(searchedTeacherCourses);

                default:
                    return Message.error("不支持的在线课堂操作类型: " + type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 添加SQL状态和错误代码信息
            String errorMsg = "数据库操作失败: " + e.getMessage();
            if (e.getSQLState() != null) {
                errorMsg += " (SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode() + ")";
            }
            return Message.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            // 添加异常类名和堆栈信息
            String errorMsg = "处理在线课堂请求失败: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            return Message.error(errorMsg);
        }
    }


    // 获取教师待办事项
    public List<String> getTeacherTodos(User currentUser) throws SQLException {
        List<String> todos = new ArrayList<>();

        // 待批改作业
        String sql1 = "SELECT a.assignment_name, COUNT(sa.student_id) as count " +
                "FROM tbl_assignment a " +
                "JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "WHERE c.teacher_id = ? AND sa.status = '已提交' AND sa.score IS NULL " +
                "GROUP BY a.assignment_name";

        // 未上传回放的课程
        String sql2 = "SELECT c.course_name FROM tbl_course c " +
                "LEFT JOIN tbl_course_playback cp ON c.course_id = cp.course_id " +
                "WHERE c.teacher_id = ? AND cp.playback_id IS NULL AND c.end_date < CURDATE()";

        // 新学生提问
        String sql3 = "SELECT COUNT(*) as count FROM tbl_discussion d " +
                "JOIN tbl_course c ON d.course_id = c.course_id " +
                "WHERE c.teacher_id = ? AND d.post_time > DATE_SUB(NOW(), INTERVAL 1 DAY) " +
                "AND d.parent_id IS NULL";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 获取待批改作业
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    todos.add("待批改作业: " + rs.getString("assignment_name") + " (" + rs.getInt("count") + "份)");
                }
            }

            // 获取未上传回放的课程
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    todos.add("课程回放待上传: " + rs.getString("course_name"));
                }
            }

            // 获取新学生提问
            try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    todos.add("新学生提问: " + rs.getInt("count") + "条未读");
                }
            }
        }

        return new ArrayList<>(todos);
    }

    // 获取用户当天的提醒
    public List<Reminder> getTodayReminders(User currentUser) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT content FROM tbl_reminder WHERE user_id = ? AND reminder_date = CURDATE()";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reminders.add(new Reminder(rs.getString("content")));
            }
        }
        return new ArrayList<>(reminders);
    }

    // 获取某个月份有提醒的日期列表
    public List<Integer> getMonthReminders(int year, int month, User currentUser) throws SQLException {
        List<Integer> daysWithReminders = new ArrayList<>();
        String sql = "SELECT DAY(reminder_date) as day FROM tbl_reminder " +
                "WHERE user_id = ? AND YEAR(reminder_date) = ? AND MONTH(reminder_date) = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                daysWithReminders.add(rs.getInt("day"));
            }
        }
        return new ArrayList<>(daysWithReminders);
    }

    // 获取某一天的提醒
    public List<Reminder> getDayReminders(int year, int month, int day, User currentUser) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT content FROM tbl_reminder WHERE user_id = ? " +
                "AND YEAR(reminder_date) = ? AND MONTH(reminder_date) = ? AND DAY(reminder_date) = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            stmt.setInt(4, day);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reminders.add(new Reminder(rs.getString("content")));
            }
        }
        return new ArrayList<>(reminders);
    }

    // 获取学生的课程列表
    public List<Course> getStudentCourses(User currentUser) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND c.is_online = 1";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // 计算课程状态
                String status = "进行中";
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                Date currentDate = new Date(System.currentTimeMillis());

                if (currentDate.before(startDate)) {
                    status = "未开始";
                } else if (currentDate.after(endDate)) {
                    status = "已结束";
                }

                Course course = new Course(
                        rs.getString("course_name"),
                        rs.getString("class_name"),
                        getTeacherName(rs.getString("teacher_id"), currentUser),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credits")
                );
                // 添加课程ID设置
                course.setCourseId(rs.getString("course_id"));
                courses.add(course);
            }
        }
        return new ArrayList<>(courses);
    }

    // 搜索课程
    public List<Course> searchCourses(String keyword, String filter, User currentUser) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND c.is_online = 1";

        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (c.course_name LIKE ? OR c.teacher_id IN (SELECT user_id FROM tbl_user WHERE real_name LIKE ?))";
        }

        // 添加筛选条件
        if (filter != null && !filter.equals("全部课程")) {
            Date currentDate = new Date(System.currentTimeMillis());
            if (filter.equals("进行中")) {
                sql += " AND c.start_date <= ? AND c.end_date >= ?";
            } else if (filter.equals("未开始")) {
                sql += " AND c.start_date > ?";
            } else if (filter.equals("已结束")) {
                sql += " AND c.end_date < ?";
            }
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, currentUser.getUserId());

            // 设置搜索参数
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(paramIndex++, likeKeyword);
                stmt.setString(paramIndex++, likeKeyword);
            }

            // 设置筛选参数
            if (filter != null && !filter.equals("全部课程")) {
                java.sql.Date currentSqlDate = new java.sql.Date(System.currentTimeMillis());
                stmt.setDate(paramIndex++, currentSqlDate);
                if (filter.equals("进行中")) {
                    stmt.setDate(paramIndex++, currentSqlDate);
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // 计算课程状态
                String status = "进行中";
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                Date currentDate = new Date(System.currentTimeMillis());

                if (currentDate.before(startDate)) {
                    status = "未开始";
                } else if (currentDate.after(endDate)) {
                    status = "已结束";
                }

                Course course = new Course(
                        rs.getString("course_name"),
                        rs.getString("class_name"),
                        getTeacherName(rs.getString("teacher_id"), currentUser),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credits")
                );
                course.setCourseId(rs.getString("course_id"));
                courses.add(course);
            }
        }
        return courses;
    }

    // 获取教师姓名
    private String getTeacherName(String teacherId, User currentUser) throws SQLException {
        String sql = "SELECT real_name FROM tbl_user WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("real_name");
            }
        }
        return "未知教师";
    }

    // 获取课程统计信息
    public CourseStats getCourseStats(User currentUser) throws SQLException {
        CourseStats stats = new CourseStats();
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN c.start_date > CURDATE() THEN 1 ELSE 0 END) as notStarted, " +
                "SUM(CASE WHEN c.start_date <= CURDATE() AND c.end_date >= CURDATE() THEN 1 ELSE 0 END) as inProgress, " +
                "SUM(CASE WHEN c.end_date < CURDATE() THEN 1 ELSE 0 END) as completed " +
                "FROM tbl_course c " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND c.is_online = 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.total = rs.getInt("total");
                stats.notStarted = rs.getInt("notStarted");
                stats.inProgress = rs.getInt("inProgress");
                stats.completed = rs.getInt("completed");
            }
        }
        return stats;
    }

    // 获取课程资料列表
    public List<CourseMaterial> getCourseMaterials(String courseId) throws SQLException {
        List<CourseMaterial> materials = new ArrayList<>();
        String sql = "SELECT material_id, material_name, type, size, upload_date FROM tbl_course_material " +
                "WHERE course_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CourseMaterial material = new CourseMaterial(
                        rs.getInt("material_id"), // 添加ID
                        rs.getString("material_name"),
                        rs.getString("type"),
                        rs.getString("size"),
                        rs.getString("upload_date")
                );
                materials.add(material);
            }
        }
        return new ArrayList<>(materials);
    }

    // 获取课程回放列表
    public List<CoursePlayback> getCoursePlaybacks(String courseId) throws SQLException {
        List<CoursePlayback> playbacks = new ArrayList<>();
        String sql = "SELECT playback_id, playback_date, title, duration FROM tbl_course_playback " +
                "WHERE course_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CoursePlayback playback = new CoursePlayback(
                        rs.getInt("playback_id"), // 添加ID
                        rs.getString("playback_date"),
                        rs.getString("title"),
                        rs.getString("duration")
                );
                playbacks.add(playback);
            }
        }
        return new ArrayList<>(playbacks);
    }

    // 获取学生作业列表
    public List<StudentAssignment> getStudentAssignments(int assignmentId, User currentUser) throws SQLException {
        List<StudentAssignment> assignments = new ArrayList<>();

        String sql = "SELECT sa.*, u.real_name, u.user_id, u.class_name " +
                "FROM tbl_student_assignment sa " +
                "JOIN tbl_user u ON sa.student_id = u.user_id " +
                "WHERE sa.assignment_id = ? " +
                "ORDER BY u.real_name";


        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(assignmentId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentAssignment sa = new StudentAssignment(
                        rs.getString("student_id"), // 修改为正确的列名
                        rs.getString("real_name"),
                        assignmentId,
                        rs.getString("status"),
                        rs.getInt("score"),
                        rs.getString("feedback"),
                        rs.getString("file_path"),
                        rs.getTimestamp("submit_time")
                );
                sa.setClassName(rs.getString("class_name"));
                assignments.add(sa);
            }
        }
        return assignments;
    }

    public List<Assignment> getStudentAssignments1(User currentUser) throws SQLException {
        return searchAssignments("", "全部作业", currentUser);
    }

    // 搜索作业
    public List<Assignment> searchAssignments(String keyword, String filter, User currentUser) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_name, c.course_name, a.assign_date, a.due_date, sa.status, sa.priority " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id " +
                "WHERE sa.student_id = ?";

        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (a.assignment_name LIKE ? OR c.course_name LIKE ?)";
        }

        // 添加筛选条件
        if (filter != null && !filter.equals("全部作业")) {
            if (filter.equals("已提交")) {
                sql += " AND sa.status = '已提交'";
            } else if (filter.equals("未提交")) {
                sql += " AND sa.status = '未提交'";
            } else if (filter.equals("紧急作业")) {
                sql += " AND sa.priority = '紧急'";
            }
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, currentUser.getUserId());

            // 设置搜索参数 - 修复：添加缺失的参数设置
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(paramIndex++, likeKeyword);
                stmt.setString(paramIndex++, likeKeyword);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String dueDateStr = rs.getString("due_date");
                String status = rs.getString("status");
                String priority = rs.getString("priority");

                // 如果作业状态是"未提交"，检查截止日期
                if ("未提交".equals(status)) {
                    // 解析截止日期
                    LocalDate dueDate = LocalDate.parse(dueDateStr);
                    LocalDate today = LocalDate.now();

                    // 计算剩余天数
                    long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

                    // 如果剩余天数小于3天，设置优先级为"紧急"
                    if (daysUntilDue < 3 && daysUntilDue >= 0) {
                        priority = "紧急";

                        // 更新数据库中的优先级
                        String updateSql = "UPDATE tbl_student_assignment SET priority = ? " +
                                "WHERE student_id = ? AND assignment_id = " +
                                "(SELECT assignment_id FROM tbl_assignment WHERE assignment_name = ?)";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, "紧急");
                            updateStmt.setString(2, currentUser.getUserId());
                            updateStmt.setString(3, rs.getString("assignment_name"));
                            updateStmt.executeUpdate();
                        }
                    }
                }

                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        dueDateStr,
                        status,
                        priority
                );
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    // 获取作业统计信息
    public AssignmentStats getAssignmentStats(User currentUser) throws SQLException {
        AssignmentStats stats = new AssignmentStats();
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN sa.status = '已提交' THEN 1 ELSE 0 END) as submitted, " +
                "SUM(CASE WHEN sa.status = '未提交' THEN 1 ELSE 0 END) as notSubmitted, " +
                "SUM(CASE WHEN sa.priority = '紧急' THEN 1 ELSE 0 END) as urgent " +
                "FROM tbl_student_assignment sa " +
                "WHERE sa.student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.total = rs.getInt("total");
                stats.submitted = rs.getInt("submitted");
                stats.notSubmitted = rs.getInt("notSubmitted");
                stats.urgent = rs.getInt("urgent");
            }
        }
        return stats;
    }

    // 提交作业
    public boolean submitAssignment(String assignmentName, String filePath, User currentUser) throws SQLException {
        String sql = "UPDATE tbl_student_assignment sa " +
                "JOIN tbl_assignment a ON sa.assignment_id = a.assignment_id " +
                "SET sa.status = '已提交', sa.submit_time = NOW(), sa.file_path = ? " +
                "WHERE sa.student_id = ? AND a.assignment_name = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filePath);
            stmt.setString(2, currentUser.getUserId());
            stmt.setString(3, assignmentName);
            return stmt.executeUpdate() > 0;
        }
    }

    // 获取最近活动
    public List<String> getRecentActivities(User currentUser) throws SQLException {
        List<String> activities = new ArrayList<>();

        // 1. 老师添加的新课程资料
        String materialsSql = "SELECT cm.material_name, c.course_name, cm.upload_date " +
                "FROM tbl_course_material cm " +
                "JOIN tbl_course c ON cm.course_id = c.course_id " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND cm.upload_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY cm.upload_date DESC LIMIT 3";

        // 2. 老师添加的新课程回放
        String playbacksSql = "SELECT cp.title, c.course_name, cp.playback_date " +
                "FROM tbl_course_playback cp " +
                "JOIN tbl_course c ON cp.course_id = c.course_id " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND cp.playback_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY cp.playback_date DESC LIMIT 3";

        // 3. 课程中的新讨论
        String discussionsSql = "SELECT d.content, c.course_name, u.real_name, d.post_time " +
                "FROM tbl_discussion d " +
                "JOIN tbl_course c ON d.course_id = c.course_id " +
                "JOIN tbl_user u ON d.user_id = u.user_id " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND d.post_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY d.post_time DESC LIMIT 3";

        // 4. 新发布的作业
        String newAssignmentsSql = "SELECT a.assignment_name, c.course_name, a.assign_date " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND a.assign_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY a.assign_date DESC LIMIT 3";

        // 5. 即将截止的作业（3天内）
        String dueAssignmentsSql = "SELECT a.assignment_name, c.course_name, a.due_date " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id AND sa.student_id = ? " +
                "WHERE sa.status = '未提交' AND a.due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 3 DAY) " +
                "ORDER BY a.due_date ASC LIMIT 3";

        try (Connection conn = DatabaseHelper.getConnection()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");

            // 查询新资料
            try (PreparedStatement stmt = conn.prepareStatement(materialsSql)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    activities.add("📄 新资料: " + rs.getString("material_name") +
                            " (" + rs.getString("course_name") + ") - " +
                            dateFormat.format(rs.getTimestamp("upload_date")));
                }
            }

            // 查询新回放
            try (PreparedStatement stmt = conn.prepareStatement(playbacksSql)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    activities.add("🎬 新回放: " + rs.getString("title") +
                            " (" + rs.getString("course_name") + ") - " +
                            dateFormat.format(rs.getTimestamp("playback_date")));
                }
            }

            // 查询新讨论
            try (PreparedStatement stmt = conn.prepareStatement(discussionsSql)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    activities.add("💬 新讨论: " + rs.getString("real_name") +
                            "在" + rs.getString("course_name") + "中发言 - " +
                            dateFormat.format(rs.getTimestamp("post_time")));
                }
            }

            // 查询新作业
            try (PreparedStatement stmt = conn.prepareStatement(newAssignmentsSql)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    activities.add("📝 新作业: " + rs.getString("assignment_name") +
                            " (" + rs.getString("course_name") + ") - " +
                            dateFormat.format(rs.getTimestamp("assign_date")));
                }
            }

            // 查询即将截止的作业
            try (PreparedStatement stmt = conn.prepareStatement(dueAssignmentsSql)) {
                stmt.setString(1, currentUser.getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    activities.add("⏰ 作业截止: " + rs.getString("assignment_name") +
                            " (" + rs.getString("course_name") + ") - " +
                            dateFormat.format(rs.getTimestamp("due_date")));
                }
            }

            // 按时间排序，取最近的5条
            activities.sort((a1, a2) -> {
                // 提取时间部分进行比较
                String time1 = a1.substring(a1.lastIndexOf("-") + 2);
                String time2 = a2.substring(a2.lastIndexOf("-") + 2);
                return time2.compareTo(time1); // 降序排序
            });

            int endIndex = Math.min(10, activities.size());
            List<String> result = new ArrayList<>(activities.subList(0, endIndex));
            return result;
        }
    }

    // 添加提醒
    public boolean addReminder(String content, Date date, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_reminder (user_id, content, reminder_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            stmt.setString(2, content);
            stmt.setDate(3, new java.sql.Date(date.getTime()));
            return stmt.executeUpdate() > 0;
        }
    }

    // 获取讨论区内容
    public List<String> getDiscussions(String courseName) throws SQLException {
        List<String> discussions = new ArrayList<>();
        String sql = "SELECT u.real_name, d.content, d.post_time FROM tbl_discussion d " +
                "JOIN tbl_user u ON d.user_id = u.user_id " +
                "WHERE d.course_id = (SELECT course_id FROM tbl_course WHERE course_name = ?) " +
                "ORDER BY d.post_time DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("real_name");
                String content = rs.getString("content");
                Timestamp time = rs.getTimestamp("post_time");
                String formattedTime = time.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                discussions.add(name + ": " + content + " - " + formattedTime);
            }
        }
        return new ArrayList<>(discussions);
    }

    // 发表讨论
    public boolean postDiscussion(String courseName, String content, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_discussion (course_id, user_id, content, post_time) " +
                "SELECT course_id, ?, ?, NOW() FROM tbl_course WHERE course_name = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            stmt.setString(2, content);
            stmt.setString(3, courseName);
            return stmt.executeUpdate() > 0;
        }
    }

    // 获取教师课程列表
    public List<Course> getTeacherCourses(User currentUser) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c WHERE c.teacher_id = ? AND c.is_online = 1";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // 计算课程状态
                String status = "进行中";
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                Date currentDate = new Date(System.currentTimeMillis());

                if (currentDate.before(startDate)) {
                    status = "未开始";
                } else if (currentDate.after(endDate)) {
                    status = "已结束";
                }

                Course course = new Course(
                        rs.getString("course_name"),
                        rs.getString("class_name"),
                        getTeacherName(rs.getString("teacher_id"), currentUser),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credits")
                );
                course.setCourseId(rs.getString("course_id")); // 设置课程ID
                courses.add(course);
            }
        }
        return new ArrayList<>(courses);
    }

    // 获取教师课程统计
    public CourseStats getTeacherCourseStats(User currentUser) throws SQLException {
        CourseStats stats = new CourseStats();
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN c.start_date > CURDATE() THEN 1 ELSE 0 END) as notStarted, " +
                "SUM(CASE WHEN c.start_date <= CURDATE() AND c.end_date >= CURDATE() THEN 1 ELSE 0 END) as inProgress, " +
                "SUM(CASE WHEN c.end_date < CURDATE() THEN 1 ELSE 0 END) as completed " +
                "FROM tbl_course c WHERE c.teacher_id = ? AND c.is_online = 1";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.total = rs.getInt("total");
                stats.notStarted = rs.getInt("notStarted");
                stats.inProgress = rs.getInt("inProgress");
                stats.completed = rs.getInt("completed");
            }
        }
        return stats;
    }

    // 获取班级列表
    public List<String> getClassNames() throws SQLException {
        List<String> classNames = new ArrayList<>();
        String sql = "SELECT DISTINCT class_name FROM tbl_user WHERE class_name IS NOT NULL AND class_name != ''";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                classNames.add(rs.getString("class_name"));
            }
        }
        return new ArrayList<>(classNames);
    }

    // 添加课程
    public boolean addCourse(Course course, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_course (course_id, course_name, teacher_id, teacher_name, department, course_type, credits, capacity, enrolled, semester, schedule, classroom, description, status, start_date, end_date, class_name, is_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String courseId = "C" + System.currentTimeMillis();
            stmt.setString(1, courseId);
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, currentUser.getUserId());
            stmt.setString(4, currentUser.getRealName());
            stmt.setString(5, "");
            stmt.setString(6, "必修");
            stmt.setInt(7, course.getCredits());
            stmt.setInt(8, 50);
            stmt.setInt(9, 0);
            stmt.setString(10, "2023-2024-1");
            stmt.setString(11, "");
            stmt.setString(12, "");
            stmt.setString(13, "");
            stmt.setString(14, "OPEN");
            stmt.setDate(15, java.sql.Date.valueOf(course.getStartDate()));
            stmt.setDate(16, java.sql.Date.valueOf(course.getEndDate()));
            stmt.setString(17, course.getClassName());

            int result = stmt.executeUpdate();
            if (result > 0) {
                // 添加学生到课程（原逻辑）
                boolean studentsAdded = addClassStudentsToCourse(courseId, course.getClassName());
                if (!studentsAdded) {
                    System.err.println("课程创建成功，但添加学生到课程失败");
                }
                return true;
            }
            return false;
        }
    }

    // 添加班级学生到课程
    private boolean addClassStudentsToCourse(String courseId, String className) throws SQLException {
        String sql = "INSERT INTO tbl_student_course (student_id, course_id) " +
                "SELECT user_id, ? FROM tbl_user WHERE class_name = ? AND role = 'student'";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, className);

            int result = stmt.executeUpdate();
            System.out.println("成功添加 " + result + " 名学生到课程 " + courseId);
            return result >= 0;
        } catch (SQLException e) {
            System.err.println("添加学生到课程失败: " + e.getMessage());
            return false;
        }
    }

    // 添加课程回放
    public boolean addPlayback(String courseId, CoursePlayback playback, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_course_playback (course_id, playback_date, title, duration, video_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setDate(2, java.sql.Date.valueOf(playback.getDate()));
            stmt.setString(3, playback.getTitle());
            stmt.setString(4, playback.getDuration());
            stmt.setString(5, playback.getVideoPath());

            return stmt.executeUpdate() > 0;
        }
    }

    // 添加课程资料
    public boolean addMaterial(String courseId, CourseMaterial material, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_course_material (course_id, material_name, type, size, upload_date, file_path) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, material.getName());
            stmt.setString(3, material.getType());
            stmt.setString(4, material.getSize());
            stmt.setDate(5, java.sql.Date.valueOf(material.getUploadDate()));
            stmt.setString(6, material.getFilePath());

            return stmt.executeUpdate() > 0;
        }
    }

    // 获取教师作业列表
    public List<Assignment> getTeacherAssignments(User currentUser) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.assignment_name, c.course_name, a.assign_date, a.due_date, " +
                "(SELECT COUNT(*) FROM tbl_student_course WHERE course_id = c.course_id) as total_students, " +
                "(SELECT COUNT(*) FROM tbl_student_assignment WHERE assignment_id = a.assignment_id AND status = '已提交') as submitted_count " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "WHERE c.teacher_id = ? " +
                "ORDER BY a.assign_date DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int totalStudents = rs.getInt("total_students");
                int submittedCount = rs.getInt("submitted_count");

                // 确定作业状态
                String status;
                if (submittedCount == 0) {
                    status = "未提交";
                } else if (submittedCount < totalStudents) {
                    status = "部分提交";
                } else {
                    status = "已提交";
                }

                String submissionInfo = submittedCount + "/" + totalStudents;

                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        rs.getString("due_date"),
                        status,
                        submissionInfo
                );
                assignment.setAssignmentId(rs.getInt("assignment_id"));
                assignments.add(assignment);
            }
        }
        return new ArrayList<>(assignments);
    }

    // 删除课程资料
    public boolean deleteMaterial(int materialId, User currentUser) throws SQLException {
        String sql = "DELETE cm FROM tbl_course_material cm " +
                "JOIN tbl_course c ON cm.course_id = c.course_id " +
                "WHERE cm.material_id = ? AND c.teacher_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            stmt.setString(2, currentUser.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    // 删除课程回放
    public boolean deletePlayback(int playbackId, User currentUser) throws SQLException {
        String sql = "DELETE cp FROM tbl_course_playback cp " +
                "JOIN tbl_course c ON cp.course_id = c.course_id " +
                "WHERE cp.playback_id = ? AND c.teacher_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playbackId);
            stmt.setString(2, currentUser.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }


    // 获取课程讨论（包括回复）
    public List<Discussion> getCourseDiscussions(String courseId) throws SQLException {
        List<Discussion> discussions = new ArrayList<>();
        String sql = "SELECT d.*, u.real_name, u.role FROM tbl_discussion d " +
                "JOIN tbl_user u ON d.user_id = u.user_id " +
                "WHERE d.course_id = ? ORDER BY d.post_time ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Discussion discussion = new Discussion(
                        rs.getInt("discussion_id"),
                        rs.getString("course_id"),
                        rs.getString("user_id"),
                        rs.getString("real_name"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("post_time"),
                        rs.getObject("parent_id") != null ?
                                String.valueOf(rs.getInt("parent_id")) : null
                );
                discussions.add(discussion);
            }
        }
        return new ArrayList<>(discussions);
    }

    // 回复讨论
    public boolean replyToDiscussion(String courseId, String parentId, String content, User currentUser) throws SQLException {
        // 检查用户是否有权限在该课程中回复
        String checkSql = "SELECT COUNT(*) FROM tbl_student_course WHERE course_id = ? AND student_id = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM tbl_course WHERE course_id = ? AND teacher_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courseId);
            checkStmt.setString(2, currentUser.getUserId());
            checkStmt.setString(3, courseId);
            checkStmt.setString(4, currentUser.getUserId());
            ResultSet rs = checkStmt.executeQuery();
            int countStudent = 0;
            int countTeacher = 0;
            if (rs.next()) {
                countStudent = rs.getInt(1);
            }
            if (rs.next()) {
                countTeacher = rs.getInt(1);
            }
            if (countStudent == 0 && countTeacher == 0) {
                // 用户不是课程的学生或教师，无权回复
                return false;
            }
        }

        // 插入回复
        String sql = "INSERT INTO tbl_discussion (discussion_id, course_id, user_id, content, post_time, parent_id) " +
                "VALUES (?, ?, ?, ?, NOW(), ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 生成唯一的讨论ID
            int discussionId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            stmt.setInt(1, discussionId);
            stmt.setString(2, courseId);
            stmt.setString(3, currentUser.getUserId());
            stmt.setString(4, content);
            stmt.setString(5, parentId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 发布作业
    public boolean publishAssignment(Assignment assignment, User currentUser) throws SQLException {
        String sql = "INSERT INTO tbl_assignment (course_id, assignment_name, assign_date, due_date, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, assignment.getCourseId());
            stmt.setString(2, assignment.getName());
            stmt.setDate(3, java.sql.Date.valueOf(assignment.getAssignDate()));
            stmt.setDate(4, java.sql.Date.valueOf(assignment.getDueDate()));
            stmt.setString(5, assignment.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取自动生成的作业ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer assignmentId = generatedKeys.getInt(1);
                        // 为选课学生创建作业记录
                        try {
                            createAssignmentForStudents(assignmentId, assignment.getCourseId());
                        } catch (SQLException e) {
                            System.err.println("为学生创建作业记录失败: " + e.getMessage());
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    // 为选课学生创建作业记录
    private boolean createAssignmentForStudents(int assignmentId, String courseId) throws SQLException {
        String sql = "INSERT INTO tbl_student_assignment (student_id, assignment_id, status) " +
                "SELECT student_id, ?, '未提交' FROM tbl_student_course WHERE course_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.setString(2, courseId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 批改作业
    public boolean gradeAssignment(String studentId, int assignmentId, int score, String feedback, User currentUser) throws SQLException {
        String sql = "UPDATE tbl_student_assignment SET score = ?, feedback = ?, status = '已批改' " +
                "WHERE student_id = ? AND assignment_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setString(2, feedback);
            stmt.setString(3, studentId);
            stmt.setInt(4, assignmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 获取课程学生列表
    public List<Student> getCourseStudents(String courseId, User currentUser) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT u.user_id, u.real_name, u.class_name, u.major " +
                "FROM tbl_user u " +
                "JOIN tbl_student_course sc ON u.user_id = sc.student_id " +
                "WHERE sc.course_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("user_id"),
                        rs.getString("real_name"),
                        rs.getString("major"),
                        "90%" // 默认出勤率
                );
                students.add(student);
            }
        }
        return new ArrayList<>(students);
    }

    // 获取教师所教课程列表（用于发布作业时选择课程）
    public List<Course> getTeacherCoursesForAssignment(User currentUser) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_name FROM tbl_course WHERE teacher_id = ? AND is_online = 1";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                courses.add(course);
            }
        }
        return new ArrayList<>(courses);
    }


    // 获取作业详情（包括提交情况）
    public Assignment getAssignmentDetails(int assignmentId, User currentUser) throws SQLException {
        String sql = "SELECT a.*, c.course_name, " +
                "(SELECT COUNT(*) FROM tbl_student_assignment WHERE assignment_id = ?) as total_students, " +
                "(SELECT COUNT(*) FROM tbl_student_assignment WHERE assignment_id = ? AND status = '已提交') as submitted_count " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "WHERE a.assignment_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.setInt(2, assignmentId);
            stmt.setInt(3, assignmentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        rs.getString("due_date"),
                        rs.getString("description")
                );
                assignment.setSubmissionInfo("已提交: " + rs.getInt("submitted_count") + "/" + rs.getInt("total_students"));
                return assignment;
            }
        }
        return null;
    }

    // 搜索教师作业
    public List<Assignment> searchTeacherAssignments(String keyword, String courseFilter, String statusFilter, User currentUser) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.assignment_name, c.course_name, a.assign_date, a.due_date, " +
                "COUNT(sa.student_id) as total_students, " +
                "SUM(CASE WHEN sa.status = '已提交' THEN 1 ELSE 0 END) as submitted_count " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "LEFT JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id " +
                "WHERE c.teacher_id = ? ";

        if (keyword == null) keyword = "";
        if (courseFilter == null) courseFilter = "所有课程";
        if (statusFilter == null) statusFilter = "所有状态";

        // 添加筛选条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND a.assignment_name LIKE ? ";
        }

        if (courseFilter != null && !courseFilter.equals("所有课程")) {
            sql += " AND c.course_name = ? ";
        }

        sql += " GROUP BY a.assignment_id, a.assignment_name, c.course_name, a.assign_date, a.due_date ";

        // 状态筛选
        if (statusFilter != null && !statusFilter.equals("所有状态")) {
            if (statusFilter.equals("待批改")) {
                sql += " HAVING submitted_count > 0 AND submitted_count < total_students ";
            } else if (statusFilter.equals("已批改")) {
                sql += " HAVING submitted_count = total_students ";
            }
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, currentUser.getUserId());

            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + keyword + "%");
            }

            if (courseFilter != null && !courseFilter.equals("所有课程")) {
                stmt.setString(paramIndex++, courseFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String submissionInfo = rs.getInt("submitted_count") + "/" + rs.getInt("total_students");
                String status = rs.getInt("submitted_count") == rs.getInt("total_students") ? "已批改" : "待批改";

                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        rs.getString("due_date"),
                        status,
                        submissionInfo
                );
                assignments.add(assignment);
            }
        }
        return new ArrayList<>(assignments);
    }

    // 获取教师所教课程名称列表
    public List<String> getTeacherCourseNames(User currentUser) throws SQLException {
        List<String> courseNames = new ArrayList<>();
        String sql = "SELECT DISTINCT course_name FROM tbl_course WHERE teacher_id = ? AND is_online = 1";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courseNames.add(rs.getString("course_name"));
            }
        }
        return new ArrayList<>(courseNames);
    }

    // 搜索教师课程
    public List<Course> searchTeacherCourses(String keyword, User currentUser) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c WHERE c.teacher_id = ? AND c.is_online = 1";


        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (c.course_name LIKE ? OR c.class_name LIKE ?)";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, currentUser.getUserId());

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(paramIndex++, likeKeyword);
                stmt.setString(paramIndex++, likeKeyword);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // 计算课程状态
                String status = "进行中";
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                Date currentDate = new Date(System.currentTimeMillis());

                if (currentDate.before(startDate)) {
                    status = "未开始";
                } else if (currentDate.after(endDate)) {
                    status = "已结束";
                }

                Course course = new Course(
                        rs.getString("course_name"),
                        rs.getString("class_name"),
                        getTeacherName(rs.getString("teacher_id"), currentUser),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credit")
                );
                course.setCourseId(rs.getString("course_id"));
                courses.add(course);
            }
        }
        return new ArrayList<>(courses);
    }
}