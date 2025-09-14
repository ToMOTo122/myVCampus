package com.vcampus.common.entity;

import java.io.Serializable;

/**
 * 客户端与服务器通信的消息类
 * 所有网络传输都使用此类封装
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息类型枚举
    public enum Type {
        // ===== 用户相关 =====
        USER_LOGIN,
        USER_LOGOUT,
        USER_REGISTER,
        USER_UPDATE,

        // ===== 学生管理 =====
        STUDENT_ADD,
        STUDENT_DELETE,
        STUDENT_UPDATE,
        STUDENT_QUERY,
        STUDENT_LIST,

        // ===== 课程管理 =====
        COURSE_ADD,
        COURSE_DELETE,
        COURSE_UPDATE,
        COURSE_QUERY,
        COURSE_LIST,
        COURSE_SELECT,
        COURSE_DROP,

        // ===== 图书馆 =====
        BOOK_SEARCH,
        BOOK_BORROW,
        BOOK_RETURN,
        BOOK_LIST,

        // ===== 教务管理（公告/消息/文件/商店等）=====
        ANNOUNCEMENT_LIST,
        ANNOUNCEMENT_DETAIL,
        ANNOUNCEMENT_ADD,
        ANNOUNCEMENT_UPDATE,
        ANNOUNCEMENT_DELETE,

        APPLICATION_SUBMIT,
        APPLICATION_LIST,
        APPLICATION_DETAIL,
        APPLICATION_APPROVE,
        APPLICATION_REJECT,

        FILE_LIST,
        FILE_DOWNLOAD,
        FILE_UPLOAD,

        SERVICE_GUIDE_LIST,
        MESSAGE_LIST,
        MESSAGE_READ,

        SHOP_LIST,
        SHOP_BUY,
        SHOP_CART,

        // ===== 论坛/BBS（你之前已添加）=====
        POST_GET_ALL,           // 请求: 获取所有帖子
        POST_CREATE,            // 请求: 创建新帖子
        POST_GET_ALL_SUCCESS,   // 响应: 成功获取所有帖子
        POST_CREATE_SUCCESS,    // 响应: 成功创建新帖子
        POST_CREATE_FAILURE,    // 响应: 创建新帖子失败

        // ===== 学籍管理（本次新增）=====
        ENROLLMENT_PROFILE_GET,      // 学生获取学籍档案（读 tbl_student_info）
        ENROLLMENT_REQUEST_SUBMIT,   // 学生提交学籍相关申请（资料变更/休学/复学/退学）
        ENROLLMENT_REQUEST_LIST,     // 列表（学生看自己的；老师/管理员看待审）
        ENROLLMENT_REQUEST_DETAIL,   // 申请详情
        ENROLLMENT_REQUEST_APPROVE,  // 审批通过（并回写主表）
        ENROLLMENT_REQUEST_REJECT,   // 审批驳回

        // ===== 通用响应类型 =====
        SUCCESS,
        ERROR,
        DATA
    }

    // 状态码枚举
    public enum Code {
        SUCCESS(200, "操作成功"),
        ERROR(500, "操作失败"),
        LOGIN_FAIL(401, "登录失败"),
        PERMISSION_DENIED(403, "权限不足"),
        NOT_FOUND(404, "数据不存在"),
        ALREADY_EXISTS(409, "数据已存在");

        private final int value;
        private final String message;

        Code(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() { return value; }
        public String getMessage() { return message; }
    }

    private String uid;           // 唯一标识符
    private Type type;            // 消息类型
    private Code code;            // 状态码
    private Object data;          // 传输数据
    private String sender;        // 发送者
    private long timestamp;       // 时间戳

    // 构造函数
    public Message() {
        this.uid = generateUID();
        this.timestamp = System.currentTimeMillis();
    }

    public Message(Type type, Object data) {
        this();
        this.type = type;
        this.data = data;
        this.code = Code.SUCCESS;
    }

    public Message(Type type, Code code, Object data) {
        this();
        this.type = type;
        this.code = code;
        this.data = data;
    }

    // 生成唯一ID
    private String generateUID() {
        return System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    // 快捷创建
    public static Message success(Object data) {
        return new Message(Type.SUCCESS, Code.SUCCESS, data);
    }
    public static Message error(String message) {
        return new Message(Type.ERROR, Code.ERROR, message);
    }
    public static Message error(Code code, String message) {
        return new Message(Type.ERROR, code, message);
    }

    // Getter/Setter
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Code getCode() { return code; }
    public void setCode(Code code) { this.code = code; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("Message[uid=%s, type=%s, code=%s, sender=%s]",
                uid, type, code, sender);
    }
}
