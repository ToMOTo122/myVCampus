package com.vcampus.server.service;

import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.Assignment;
import com.vcampus.common.entity.CourseMaterial;
import com.vcampus.common.entity.CoursePlayback;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OnlineClassService {
    private User currentUser;

    public OnlineClassService(User user) {
        this.currentUser = user;
    }

    // 获取用户当天的提醒
    public List<Reminder> getTodayReminders() throws SQLException {
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
        return reminders;
    }

    // 获取某个月份有提醒的日期列表
    public List<Integer> getMonthReminders(int year, int month) throws SQLException {
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
        return daysWithReminders;
    }

    // 获取某一天的提醒
    public List<Reminder> getDayReminders(int year, int month, int day) throws SQLException {
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
        return reminders;
    }

    // 获取学生的课程列表
    public List<Course> getStudentCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND c.is_online = 1";


        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

        }

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
                        getTeacherName(rs.getString("teacher_id")),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credit")
                );
                courses.add(course);
            }
        }
        return courses;
    }

    // 搜索课程
    public List<Course> searchCourses(String keyword, String filter) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM tbl_course c " +
                "JOIN tbl_student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND c.is_online = 1";

        // 添加搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (c.course_name LIKE ? OR c.teacher_id IN (SELECT user_id FROM tbl_user WHERE realName LIKE ?))";
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
            if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(paramIndex++, likeKeyword);
                stmt.setString(paramIndex++, likeKeyword);
            }

            // 设置筛选参数
            if (filter != null && !filter.equals("全部课程")) {
                stmt.setDate(paramIndex++, new java.sql.Date(System.currentTimeMillis()));
                if (filter.equals("进行中")) {
                    stmt.setDate(paramIndex++, new java.sql.Date(System.currentTimeMillis()));
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
                        getTeacherName(rs.getString("teacher_id")),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        status,
                        rs.getInt("credit")
                );
                courses.add(course);
            }
        }
        return courses;
    }

    // 获取教师姓名
    private String getTeacherName(String teacherId) throws SQLException {
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
    public CourseStats getCourseStats() throws SQLException {
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

    // 获取课程回放列表
    public List<CoursePlayback> getCoursePlaybacks(String courseName) throws SQLException {
        List<CoursePlayback> playbacks = new ArrayList<>();
        String sql = "SELECT playback_date, title, duration FROM tbl_course_playback " +
                "WHERE course_id = (SELECT course_id FROM tbl_course WHERE course_name = ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CoursePlayback playback = new CoursePlayback(
                        rs.getString("playback_date"),
                        rs.getString("title"),
                        rs.getString("duration")
                );
                playbacks.add(playback);
            }
        }
        return playbacks;
    }

    // 获取课程资料列表
    public List<CourseMaterial> getCourseMaterials(String courseName) throws SQLException {
        List<CourseMaterial> materials = new ArrayList<>();
        String sql = "SELECT material_name, type, size, upload_date FROM tbl_course_material " +
                "WHERE course_id = (SELECT course_id FROM tbl_course WHERE course_name = ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CourseMaterial material = new CourseMaterial(
                        rs.getString("material_name"),
                        rs.getString("type"),
                        rs.getString("size"),
                        rs.getString("upload_date")
                );
                materials.add(material);
            }
        }
        return materials;
    }

    // 获取学生作业列表
    public List<Assignment> getStudentAssignments() throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_name, c.course_name, a.assign_date, a.due_date, sa.status, sa.priority " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id " +
                "WHERE sa.student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        rs.getString("due_date"),
                        rs.getString("status"),
                        rs.getString("priority")
                );
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    // 搜索作业
    public List<Assignment> searchAssignments(String keyword, String filter) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_name, c.course_name, a.assign_date, a.due_date, sa.status, sa.priority " +
                "FROM tbl_assignment a " +
                "JOIN tbl_course c ON a.course_id = c.course_id " +
                "JOIN tbl_student_assignment sa ON a.assignment_id = sa.assignment_id " +
                "WHERE sa.student_id = ?";

        // 添加搜索条件
        if (keyword != null && !keyword.isEmpty()) {
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

            // 设置搜索参数
            if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(paramIndex++, likeKeyword);
                stmt.setString(paramIndex++, likeKeyword);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Assignment assignment = new Assignment(
                        rs.getString("assignment_name"),
                        rs.getString("course_name"),
                        rs.getString("assign_date"),
                        rs.getString("due_date"),
                        rs.getString("status"),
                        rs.getString("priority")
                );
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    // 获取作业统计信息
    public AssignmentStats getAssignmentStats() throws SQLException {
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
    public boolean submitAssignment(String assignmentName, String filePath) throws SQLException {
        String sql = "UPDATE tbl_student_assignment SET status = '已提交', submit_time = NOW(), file_path = ? " +
                "WHERE student_id = ? AND assignment_id = " +
                "(SELECT assignment_id FROM tbl_assignment WHERE assignment_name = ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filePath);
            stmt.setString(2, currentUser.getUserId());
            stmt.setString(3, assignmentName);
            return stmt.executeUpdate() > 0;
        }
    }

    // 获取最近活动
    public List<String> getRecentActivities() throws SQLException {
        List<String> activities = new ArrayList<>();
        String sql = "SELECT activity_content, activity_time FROM tbl_recent_activity " +
                "WHERE user_id = ? ORDER BY activity_time DESC LIMIT 5";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp time = rs.getTimestamp("activity_time");
                String content = rs.getString("activity_content");
                activities.add(new SimpleDateFormat("MM-dd HH:mm").format(time) + " - " + content);
            }
        }
        return activities;
    }

    // 添加提醒
    public boolean addReminder(String content, Date date) throws SQLException {
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
                discussions.add(name + ": " + content + " - " +
                        new SimpleDateFormat("MM-dd HH:mm").format(time));
            }
        }
        return discussions;
    }

    // 发表讨论
    public boolean postDiscussion(String courseName, String content) throws SQLException {
        String sql = "INSERT INTO tbl_discussion (course_id, user_id, content, post_time) " +
                "VALUES ((SELECT course_id FROM tbl_course WHERE course_name = ?), ?, ?, NOW())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseName);
            stmt.setString(2, currentUser.getUserId());
            stmt.setString(3, content);
            return stmt.executeUpdate() > 0;
        }
    }

    // 内部类：提醒
    public static class Reminder {
        private String content;

        public Reminder(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    // 内部类：课程统计
    public static class CourseStats {
        public int total;
        public int notStarted;
        public int inProgress;
        public int completed;
    }

    // 内部类：作业统计
    public static class AssignmentStats {
        public int total;
        public int submitted;
        public int notSubmitted;
        public int urgent;
    }
}