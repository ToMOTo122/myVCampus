package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;
import com.vcampus.server.dao.EnrollmentDao;

import java.sql.*;
import java.util.*;

/**
 * 学籍管理 Service（覆盖原文件即可）
 *
 * 支持的消息类型：
 *  ENROLLMENT_PROFILE_GET
 *  ENROLLMENT_REQUEST_SUBMIT
 *  ENROLLMENT_REQUEST_LIST
 *  ENROLLMENT_REQUEST_DETAIL
 *  ENROLLMENT_REQUEST_APPROVE
 *  ENROLLMENT_REQUEST_REJECT
 */
public class EnrollmentService {
    private final EnrollmentDao dao = new EnrollmentDao();

    // ================== 入口 ==================
    public Message handle(Message msg, User currentUser) {
        try {
            if (currentUser == null) {
                return Message.error("请先登录");
            }

            switch (msg.getType()) {
                case ENROLLMENT_PROFILE_GET:
                    return onGetProfile(currentUser);

                case ENROLLMENT_REQUEST_SUBMIT:
                    return onSubmit(msg, currentUser);

                case ENROLLMENT_REQUEST_LIST:
                    return onList(msg, currentUser);

                case ENROLLMENT_REQUEST_DETAIL:
                    return onDetail(msg, currentUser);

                case ENROLLMENT_REQUEST_APPROVE:
                    return onApprove(msg, currentUser);

                case ENROLLMENT_REQUEST_REJECT:
                    return onReject(msg, currentUser);

                default:
                    return Message.error("不支持的学籍操作: " + msg.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error("学籍服务异常: " + e.getMessage());
        }
    }

    // ================== handlers ==================

    /** 学籍档案（学生） */
    private Message onGetProfile(User currentUser) throws SQLException {
        String studentId = currentUser.getUserId();
        Map<String, Object> profile = null;

        try (Connection c = DatabaseHelper.getConnection()) {
            String sql = "SELECT student_id, admission_year, graduation_year, gpa, total_credits, status, advisor_id " +
                    "FROM tbl_student_info WHERE student_id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        profile = new LinkedHashMap<>();
                        profile.put("studentId",   rs.getString("student_id"));
                        profile.put("enrollYear",  rs.getObject("admission_year"));
                        profile.put("graduateYear",rs.getObject("graduation_year"));
                        profile.put("gpa",         rs.getObject("gpa"));
                        profile.put("totalScore",  rs.getObject("total_credits"));
                        profile.put("status",      toCnStatus(rs.getString("status")));
                        profile.put("advisorId",   rs.getString("advisor_id"));
                    }
                }
            }
        }

        if (profile == null) {
            // 返回一个默认档案，避免前端空指针
            profile = new LinkedHashMap<>();
            profile.put("studentId",   studentId);
            profile.put("enrollYear",  2023);
            profile.put("graduateYear",2027);
            profile.put("gpa",         "-");
            profile.put("totalScore",  "-");
            profile.put("status",      "在读");
            profile.put("advisorId",   "");
        }

        return Message.success(profile);
    }

    /** 提交学籍相关申请（学生） */
    private Message onSubmit(Message msg, User currentUser) throws Exception {
        if (!currentUser.isStudent()) {
            return Message.error("只有学生可以提交学籍申请");
        }
        Map<String, Object> data = asMap(msg.getData());
        String changeType = str(data, "changeType", null);
        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) data.get("changes");
        String reason = str(data, "reason", "");

        if (changeType == null || changes == null) {
            return Message.error("缺少参数：changeType / changes");
        }

        // 兼容：如果前端传中文状态，转为库里的英文枚举
        Object st = changes.get("status");
        if (st != null) {
            changes.put("status", toDbStatus(String.valueOf(st)));
        }

        long id;
        try (Connection c = DatabaseHelper.getConnection()) {
            id = dao.submitChange(c, currentUser.getUserId(), changeType, changes, reason);
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("changeId", id);
        return Message.success(ret);
    }

    /** 申请列表（学生=我的；老师/管理员=待审） */
    private Message onList(Message msg, User currentUser) throws Exception {
        Map<String, Object> data = asMap(msg.getData());
        List<Map<String, Object>> raw;
        try (Connection c = DatabaseHelper.getConnection()) {
            if (currentUser.isStudent()) {
                raw = dao.listForUser(c, currentUser.getUserId());
            } else {
                // 管理端默认看待审列表（忽略传入的 status 以保持简单）
                raw = dao.listPending(c);
            }
        }

        // 字段名转换给前端
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : raw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",         r.get("id"));
            m.put("studentId",  r.get("student_id"));
            m.put("changeType", r.get("change_type"));
            m.put("status",     r.get("status"));
            m.put("reason",     r.get("reason"));
            m.put("submitTime", r.get("submit_time"));
            out.add(m);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("requests", out);
        return Message.success(resp);
    }

    /** 申请详情（老师/管理员） */
    private Message onDetail(Message msg, User currentUser) throws Exception {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("只有老师或管理员可以查看详情");
        }
        Map<String, Object> data = asMap(msg.getData());
        long changeId = longv(data, "id", -1L);
        if (changeId <= 0) return Message.error("缺少参数：id");

        try (Connection c = DatabaseHelper.getConnection()) {
            Map<String, Object> d = dao.detail(c, changeId);
            if (d == null || d.isEmpty()) return Message.error("申请不存在");

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id",         d.get("id"));
            out.put("studentId",  d.get("student_id"));
            out.put("changeType", d.get("change_type"));
            out.put("status",     d.get("status"));
            out.put("reason",     d.get("reason"));
            out.put("submitTime", d.get("submit_time"));
            out.put("changes",    d.get("payload")); // JSON 字符串
            return Message.success(out);
        }
    }

    /** 审批通过 */
    private Message onApprove(Message msg, User currentUser) throws Exception {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("只有老师或管理员可以审批");
        }
        Map<String, Object> data = asMap(msg.getData());
        long changeId = longv(data, "id", -1L);
        String comment = str(data, "comment", "");
        if (changeId <= 0) return Message.error("缺少参数：id");

        try (Connection c = DatabaseHelper.getConnection()) {
            dao.approve(c, changeId, currentUser.getUserId(), comment);
        }
        return Message.success("OK");
    }

    /** 审批拒绝 */
    private Message onReject(Message msg, User currentUser) throws Exception {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            return Message.error("只有老师或管理员可以审批");
        }
        Map<String, Object> data = asMap(msg.getData());
        long changeId = longv(data, "id", -1L);
        String comment = str(data, "comment", "");
        if (changeId <= 0) return Message.error("缺少参数：id");

        try (Connection c = DatabaseHelper.getConnection()) {
            dao.reject(c, changeId, currentUser.getUserId(), comment);
        }
        return Message.success("OK");
    }

    // ================== 工具方法 ==================

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : new HashMap<>();
    }

    private String str(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return v == null ? def : String.valueOf(v);
    }

    private long longv(Map<String, Object> m, String k, long def) {
        Object v = m.get(k);
        if (v == null) return def;
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception ignore) { return def; }
    }

    // 状态值：库<->中文
    private static String toCnStatus(String db) {
        if (db == null) return "在读";
        switch (db) {
            case "ACTIVE":     return "在读";
            case "SUSPENDED":  return "休学";
            case "EXPELLED":   return "退学";
            case "GRADUATED":  return "已毕业";
            default:           return db;
        }
    }

    private static String toDbStatus(String cn) {
        if (cn == null) return null;
        switch (cn) {
            case "在读":   return "ACTIVE";
            case "休学":   return "SUSPENDED";
            case "退学":   return "EXPELLED";
            case "已毕业": return "GRADUATED";
            default:       return cn;
        }
    }
}
