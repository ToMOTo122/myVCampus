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
        // 用户相关
        USER_LOGIN,
        USER_LOGOUT,
        USER_REGISTER,
        USER_UPDATE,

        // 学生管理
        STUDENT_ADD,
        STUDENT_DELETE,
        STUDENT_UPDATE,
        STUDENT_QUERY,
        STUDENT_LIST,

        // 课程管理
        COURSE_ADD,
        COURSE_DELETE,
        COURSE_UPDATE,
        COURSE_QUERY,
        COURSE_LIST,
        COURSE_SELECT,
        COURSE_DROP,

        // 图书馆
        BOOK_SEARCH,
        BOOK_BORROW,
        BOOK_RETURN,
        BOOK_LIST,

        // 教务管理相关
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


        FILE_LIST,             // 获取文件列表
        FILE_DOWNLOAD,         // 下载文件
        FILE_UPLOAD,           // 上传文件


        SERVICE_GUIDE_LIST,     // 获取办事指南
        MESSAGE_LIST,          // 获取消息列表
        MESSAGE_READ,          // 标记消息已读

        // 商店
        SHOP_LIST,
        SHOP_BUY,
        SHOP_CART,

        // 响应类型
        SUCCESS,
        ERROR,
        ANNOUNCEMENT_PUBLISH, FILE_DELETE, DATA
    }

    // 状态码枚举
    public enum Code {
        SUCCESS(200, "操作成功"),
        ERROR(500, "操作失败"),
        LOGIN_FAIL(401, "登录失败"),
        PERMISSION_DENIED(403, "权限不足"),
        NOT_FOUND(404, "数据不存在"),
        ALREADY_EXISTS(409, "数据已存在"),
        INVALID_CREDENTIALS(401, "无效凭证"),
        USER_NOT_FOUND(404, "用户未找到"),
        INVALID_INPUT(400, "输入无效"),
        DUPLICATE_ENTRY(409, "重复条目"),
        ANNOUNCEMENT_PUBLISH(200, "公告发布操作已完成"),
        SERVER_ERROR(500, "服务器内部错误");     // 服务器内部错误

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

    // 创建成功响应
    public static Message success(Object data) {
        return new Message(Type.SUCCESS, Code.SUCCESS, data);
    }

    // 创建错误响应
    public static Message error(String message) {
        return new Message(Type.ERROR, Code.ERROR, message);
    }

    public static Message error(Code code, String message) {
        return new Message(Type.ERROR, code, message);
    }

    // Getter和Setter方法
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