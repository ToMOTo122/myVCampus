package com.vcampus.server.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;

/**
 * 学籍管理 DAO（与 script.sql 对齐）
 *
 * 表：
 *  - tbl_student_info(student_id PK, student_no, admission_year, graduation_year, advisor_id, status, gpa, total_credits)
 *  - tbl_enrollment_change(
 *        id BIGINT PK AI,
 *        student_id VARCHAR(20) NOT NULL,
 *        payload JSON NOT NULL,
 *        reason VARCHAR(255),
 *        status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
 *        submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 *        reviewed_by VARCHAR(20),
 *        reviewed_at DATETIME,
 *        review_comment VARCHAR(255)
 *    )
 *  - tbl_enrollment_change_history(
 *        id BIGINT PK AI,
 *        change_id BIGINT NOT NULL,
 *        action ENUM('SUBMIT','APPROVE','REJECT') NOT NULL,
 *        actor_id VARCHAR(20) NOT NULL,
 *        action_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 *        comment VARCHAR(255),
 *        payload JSON,
 *        FOREIGN KEY(change_id) REFERENCES tbl_enrollment_change(id)
 *    )
 */
public class EnrollmentDao {

    private static final String CHANGES_COL = "payload";

    /** === 学籍档案 === */
    public Map<String,Object> getProfile(Connection c, String studentId) throws SQLException {
        String sql = "SELECT " +
                " student_id, " +
                " student_no, " +
                " admission_year  AS enroll_year, " +
                " graduation_year AS graduate_year, " +
                " advisor_id, " +
                " status, " +
                " gpa, " +
                " total_credits   AS total_score " +
                "FROM tbl_student_info WHERE student_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("student_id", studentId);
                    m.put("student_no", "");
                    m.put("enroll_year", null);
                    m.put("graduate_year", null);
                    m.put("advisor_id", "");
                    m.put("status", "ACTIVE");
                    m.put("gpa", null);
                    m.put("total_score", null);
                    return m;
                }
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("student_id", rs.getString("student_id"));
                m.put("student_no", rs.getString("student_no"));
                m.put("enroll_year", rs.getObject("enroll_year"));
                m.put("graduate_year", rs.getObject("graduate_year"));
                m.put("advisor_id", rs.getString("advisor_id"));
                m.put("status", rs.getString("status"));
                m.put("gpa", rs.getObject("gpa"));
                m.put("total_score", rs.getObject("total_score"));
                return m;
            }
        }
    }

    /** === 提交申请（返回 change_id）=== */
    public long submitChange(Connection c,
                             String studentId,
                             String changeType,
                             Map<String,Object> changes,
                             String reason) throws SQLException {
        String insertSql = "INSERT INTO tbl_enrollment_change " +
                " (student_id, status, reason, " + CHANGES_COL + ") " +
                " VALUES (?, 'PENDING', ?, CAST(? AS JSON))";
        try (PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            // 将 changeType 合并进 JSON
            Map<String,Object> payload = new LinkedHashMap<>();
            if (changes != null) payload.putAll(changes);
            payload.put("changeType", changeType);
            String json = toJsonString(payload);

            ps.setString(1, studentId);
            ps.setString(2, reason);
            ps.setString(3, json);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                long id = rs.getLong(1);
                insertHistory(c, id, "SUBMIT", studentId, reason, json);
                return id;
            }
        }
    }

    /** === 学生-我的申请列表 === */
    public List<Map<String,Object>> listForUser(Connection c, String studentId) throws SQLException {
        String sql = "SELECT id, student_id, " +
                " JSON_UNQUOTE(JSON_EXTRACT(IFNULL(" + CHANGES_COL + ", JSON_OBJECT()), '$.changeType')) AS change_type, " +
                " status, reason, submitted_at AS submit_time " +
                " FROM tbl_enrollment_change WHERE student_id=? " +
                " ORDER BY submitted_at DESC, id DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return readRows(rs);
            }
        }
    }

    /** === 待审列表（老师/管理员） === */
    public List<Map<String,Object>> listPending(Connection c) throws SQLException {
        String sql = "SELECT id, student_id, " +
                " JSON_UNQUOTE(JSON_EXTRACT(IFNULL(" + CHANGES_COL + ", JSON_OBJECT()), '$.changeType')) AS change_type, " +
                " status, reason, submitted_at AS submit_time " +
                " FROM tbl_enrollment_change WHERE status='PENDING' " +
                " ORDER BY submitted_at DESC, id DESC";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return readRows(rs);
        }
    }

    /** === 详情 === */
    public Map<String,Object> detail(Connection c, long changeId) throws SQLException {
        String sql = "SELECT id, student_id, " +
                " JSON_UNQUOTE(JSON_EXTRACT(IFNULL(" + CHANGES_COL + ", JSON_OBJECT()), '$.changeType')) AS change_type, " +
                " status, reason, submitted_at AS submit_time, " +
                " IFNULL(" + CHANGES_COL + ", NULL) AS payload " +
                " FROM tbl_enrollment_change WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, changeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Collections.emptyMap();
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("id"));
                m.put("student_id", rs.getString("student_id"));
                m.put("change_type", rs.getString("change_type"));
                m.put("status", rs.getString("status"));
                m.put("reason", rs.getString("reason"));
                m.put("submit_time", rs.getTimestamp("submit_time"));
                m.put("payload", rs.getString("payload")); // JSON
                return m;
            }
        }
    }

    /** === 审批通过 === */
    public void approve(Connection c, long changeId, String reviewerId, String comment) throws SQLException {
        Map<String,Object> change = detail(c, changeId);
        if (change.isEmpty()) throw new SQLException("变更不存在: " + changeId);
        if (!"PENDING".equalsIgnoreCase(String.valueOf(change.get("status")))) {
            throw new SQLException("该申请已处理");
        }
        String studentId = (String) change.get("student_id");
        String changeType = (String) change.get("change_type");
        String payload = (String) change.get("payload");

        // 业务应用
        if ("INFO_UPDATE".equalsIgnoreCase(changeType)) {
            JsonObject obj = parseJsonObject(payload);
            obj.remove("changeType"); // 去掉自身
            applyToStudentInfo(c, studentId, obj);
        } else if ("SUSPEND".equalsIgnoreCase(changeType)) {
            updateStudentStatus(c, studentId, "SUSPENDED");
        } else if ("DROP_OUT".equalsIgnoreCase(changeType)) {
            updateStudentStatus(c, studentId, "EXPELLED"); // 与脚本枚举一致
        } else if ("RESUME".equalsIgnoreCase(changeType)) {
            updateStudentStatus(c, studentId, "ACTIVE");
        }

        // 更新主表
        String sql = "UPDATE tbl_enrollment_change " +
                " SET status='APPROVED', reviewed_by=?, review_comment=?, reviewed_at=NOW() " +
                " WHERE id=? AND status='PENDING'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reviewerId);
            ps.setString(2, comment);
            ps.setLong(3, changeId);
            ps.executeUpdate();
        }

        insertHistory(c, changeId, "APPROVE", reviewerId, comment, payload);
    }

    /** === 审批驳回 === */
    public void reject(Connection c, long changeId, String reviewerId, String comment) throws SQLException {
        Map<String,Object> change = detail(c, changeId);
        if (change.isEmpty()) throw new SQLException("变更不存在: " + changeId);
        if (!"PENDING".equalsIgnoreCase(String.valueOf(change.get("status")))) {
            throw new SQLException("该申请已处理");
        }
        String payload = (String) change.get("payload");

        String sql = "UPDATE tbl_enrollment_change " +
                " SET status='REJECTED', reviewed_by=?, review_comment=?, reviewed_at=NOW() " +
                " WHERE id=? AND status='PENDING'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reviewerId);
            ps.setString(2, comment);
            ps.setLong(3, changeId);
            ps.executeUpdate();
        }

        insertHistory(c, changeId, "REJECT", reviewerId, comment, payload);
    }

    // ================= 内部工具 =================

    /** 审批历史写入 */
    private void insertHistory(Connection c, long changeId, String action,
                               String actorId, String comment, Object payload) throws SQLException {
        String sql = "INSERT INTO tbl_enrollment_change_history " +
                " (change_id, action, actor_id, comment, payload) " +
                " VALUES (?, ?, ?, ?, CAST(? AS JSON))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, changeId);
            ps.setString(2, action);
            ps.setString(3, actorId);
            ps.setString(4, comment);
            ps.setString(5, payload == null ? null : payload.toString());
            ps.executeUpdate();
        }
    }

    /** 更新学生状态 */
    private void updateStudentStatus(Connection c, String studentId, String status) throws SQLException {
        String sql = "INSERT INTO tbl_student_info (student_id, status) VALUES (?, ?) " +
                " ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, status);
            ps.executeUpdate();
        }
    }

    /** 解析 JSON 为 JsonObject（失败返回空对象） */
    private JsonObject parseJsonObject(String json) {
        try {
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement el = parser.parse(json);
            if (el != null && el.isJsonObject()) return el.getAsJsonObject();
        } catch (Exception ignored) {}
        return new JsonObject();
    }

    /** 应用变更字段到学生信息表 */
    private void applyToStudentInfo(Connection c, String studentId, JsonObject p) throws SQLException {
        Map<String, String> colMap = new LinkedHashMap<>();
        colMap.put("enroll_year",   "admission_year");
        colMap.put("graduate_year", "graduation_year");
        colMap.put("total_score",   "total_credits");
        colMap.put("advisor_id",    "advisor_id");
        colMap.put("status",        "status");
        colMap.put("gpa",           "gpa");
        colMap.put("student_no",    "student_no");

        List<String> sets = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : p.entrySet()) {
            String k = e.getKey();
            if (!colMap.containsKey(k)) continue;
            String col = colMap.get(k);
            JsonElement v = e.getValue();
            sets.add(col + "=?");
            if (v == null || v.isJsonNull()) {
                values.add(null);
            } else if (v.isJsonPrimitive()) {
                if (v.getAsJsonPrimitive().isNumber()) {
                    values.add(v.getAsNumber());
                } else {
                    values.add(v.getAsString());
                }
            } else {
                values.add(v.toString());
            }
        }

        if (sets.isEmpty()) return;

        String sql = "INSERT INTO tbl_student_info (student_id) VALUES (?) " +
                " ON DUPLICATE KEY UPDATE " + String.join(", ", sets);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            int idx = 2;
            for (Object v : values) {
                ps.setObject(idx++, v);
            }
            ps.executeUpdate();
        }
    }

    /** 读取结果集 -> 列表（按列别名输出） */
    private List<Map<String,Object>> readRows(ResultSet rs) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        while (rs.next()) {
            Map<String,Object> m = new LinkedHashMap<>();
            for (int i=1;i<=cols;i++) {
                m.put(md.getColumnLabel(i), rs.getObject(i));
            }
            list.add(m);
        }
        return list;
    }

    /** Map -> JSON 字符串（最小实现） */
    private String toJsonString(Map<String,Object> map) {
        if (map == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String,Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append('"').append(escape(e.getKey())).append('"').append(":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(String.valueOf(v));
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
