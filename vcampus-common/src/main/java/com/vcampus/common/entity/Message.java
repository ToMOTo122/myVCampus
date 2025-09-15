package com.vcampus.common.entity;



import java.io.Serializable;

import java.io.Serializable;

import java.util.UUID;

/**

 * 客户端与服务器通信的消息类

 * 所有网络传输都使用此类封装

 */

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String BOOK_RENEW = "book_renew";

    public static final String BOOK_ADD = "book_add";

    public static final String BOOK_DELETE = "book_delete";

    public static final String BOOK_UPDATE = "book_update";

    public static final String BORROW_RECORD_LIST = "borrow_record_list";

    public static final String BORROW_RECORD_GET_ALL = "borrow_record_get_all"; // 确保存在

    public static final String BORROW_RECORD_RETURN = "borrow_record_return"; // 确保存在

    public static final String BOOK_GET_ALL = "book_get_all"; // 确保存在

//private static final long serialVersionUID = 1L;



    private String id; // 消息ID，用于唯一标识

    private Type type; // 消息类型，用于识别服务

    private String senderId; // 发送者ID

    private Object data; // 携带的数据对象

    private String returnType; // 这是一个 String，我们稍后处理它

    //yhr9.14 22：47添加
    private boolean success;
    // 添加这个构造函数，用于创建成功或失败的响应
    public Message(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }
    // 添加这个方法
    public boolean isSuccess() {
        return success;
    }

    // 添加这个方法
    public void setSuccess(boolean success) {
        this.success = success;
    }

// 之前可能已有的构造函数，例如：

    public Message(Type type, String senderId, Object data) {

        this.id = UUID.randomUUID().toString();

        this.type = type;

        this.senderId = senderId;

        this.data = data;

    }



// ★ 关键修改: 添加一个只接受 Type 类型参数的构造函数

    public Message(Type type) {

        this.id = UUID.randomUUID().toString();

        this.type = type;

// 可以在这里设置默认的 senderId 或 data

        this.senderId = "system";

        this.data = null;

    }

// 消息类型枚举

    public enum Type {

// 用户相关

        USER_LOGIN,

        USER_LOGOUT,

        USER_REGISTER,

        USER_UPDATE,

// 新增选课系统相关类型

        COURSE_LIST, // 获取课程列表

        STUDENT_SELECTED_COURSES, // 获取学生已选课程

        SELECT_COURSE, // 选择课程

        DROP_COURSE, // 退选课程

        CHECK_COURSE_CONFLICT, // 检查课程冲突

        TEACHER_COURSES, // 获取教师课程

        COURSE_STUDENTS, // 获取课程学生列表

        ADD_COURSE, // 添加课程

        UPDATE_COURSE, // 更新课程

        DELETE_COURSE, // 删除课程

        STUDENT_SCHEDULE, // 获取学生课表

        COURSE_DETAIL, // 获取课程详情

        SCHEDULE_CONFLICT_CHECK, // 排课冲突检查

        BATCH_COURSE_OPERATION, // 批量课程操作

        COURSE_STATISTICS, // 课程统计信息

// 学生管理

        STUDENT_ADD,

        STUDENT_DELETE,

        STUDENT_UPDATE,

        STUDENT_QUERY,

        STUDENT_LIST,



        ALL_COURSES, // 获取所有课程



        TEACHER_LIST, // 获取教师列表

// 课程管理

        COURSE_ADD,

        COURSE_DELETE,

        COURSE_UPDATE,

        COURSE_QUERY,



        COURSE_SELECT,

        COURSE_DROP,



// 图书馆

        BOOK_SEARCH,

        BOOK_BORROW,

        BOOK_RETURN,

        BOOK_LIST,

        // 商店
        SHOP_LIST,
        SHOP_BUY,
        SHOP_CART,
        //yhr9.14 9：40新增
        SEARCH_PRODUCT,     // 新增
        ADD_TO_CART,        // 新增
        GET_CART_COUNT,     // 新增
        GET_CART_ITEMS,     // 新增
        UPDATE_CART_ITEM,   // 新增
        REMOVE_CART_ITEM,   // 新增
        CHECKOUT,            // 新增

        //yhr9.14 22：18新增
        // 二手商店
        SECOND_HAND_LIST,
        SECOND_HAND_SEARCH,
        SECOND_HAND_POST,
        SECOND_HAND_MY_POSTS,
        SECOND_HAND_WANT,
        SECOND_HAND_MY_WANTS,
        SECOND_HAND_REMOVE_WANT,
        // 新增的商城消息类型 yhr9.14 1：39
        GET_ALL_PRODUCTS,     // 获取所有商品列表
        GET_PRODUCT_BY_ID,    // 根据ID获取商品详情
        UPDATE_PRODUCT_STOCK, // 更新商品库存
        CLEAR_CART,           // 清空购物车
        GET_USER_ORDERS,      // 获取用户所有订单
        //添加完毕

        // 订单管理 yhr9.14 10：40添加下面三行
        ORDER_LIST,
        ORDER_DETAIL,
        ORDER_CANCEL,

// 新增的类型

        BOOK_ADD,

        BOOK_DELETE,

        BOOK_UPDATE,

        BOOK_RENEW,

        BORROW_HISTORY,

        BORROW_RECORD_LIST,// <-- 新增：获取借阅历史记录

        BORROW_RECORD_GET_ALL,

        BORROW_RECORD_RETURN,

        BOOK_GET_ALL,

        GET_BORROW_RECORD,

        GET_BORROW_RECORD_SUCCESS,

        SEARCH_BOOK,

        SEARCH_BOOK_SUCCESS,

        GET_BOOK_LIST,

        GET_BOOK_LIST_SUCCESS,

        GET_BORROW_RECORD_LIST,

        BOOK_ADD_SUCCESS,

        RETURN_BOOK,

        BOOK_UPDATE_SUCCESS,

        BOOK_DELETE_SUCCESS,

        BORROW_RECORD_LIST_SUCCESS,

// 教务管理相关

        ANNOUNCEMENT_LIST,

        ANNOUNCEMENT_DETAIL,

        ANNOUNCEMENT_ADD,

        ANNOUNCEMENT_UPDATE,

        ANNOUNCEMENT_DELETE,
        COURSE_SELECTIONS,
        USER_LIST,



        APPLICATION_SUBMIT,

        APPLICATION_LIST,

        APPLICATION_DETAIL,

        APPLICATION_APPROVE,

        APPLICATION_REJECT,
        PING,




        FILE_LIST, // 获取文件列表

        FILE_DOWNLOAD, // 下载文件

        FILE_UPLOAD, // 上传文件





        SERVICE_GUIDE_LIST, // 获取办事指南

        MESSAGE_LIST, // 获取消息列表

        MESSAGE_READ, // 标记消息已读




// 响应类型

        SUCCESS,

        ERROR,

        LOGIN,

        DATA,

// 在线课堂
        ONLINE_CLASS_GET_TEACHER_TODOS,
        ONLINE_CLASS_GET_TODAY_REMINDERS,
        ONLINE_CLASS_GET_MONTH_REMINDERS,
        ONLINE_CLASS_GET_DAY_REMINDERS,
        ONLINE_CLASS_GET_STUDENT_COURSES,
        ONLINE_CLASS_SEARCH_COURSES,
        ONLINE_CLASS_GET_COURSE_STATS,
        ONLINE_CLASS_GET_COURSE_MATERIALS,
        ONLINE_CLASS_GET_COURSE_PLAYBACKS,
        ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS,
        ONLINE_CLASS_SEARCH_ASSIGNMENTS,
        ONLINE_CLASS_GET_ASSIGNMENT_STATS,
        ONLINE_CLASS_SUBMIT_ASSIGNMENT,
        ONLINE_CLASS_GET_RECENT_ACTIVITIES,
        ONLINE_CLASS_ADD_REMINDER,
        ONLINE_CLASS_GET_DISCUSSIONS,
        ONLINE_CLASS_POST_DISCUSSION,
        ONLINE_CLASS_GET_TEACHER_COURSES,
        ONLINE_CLASS_GET_TEACHER_COURSE_STATS,
        ONLINE_CLASS_ADD_COURSE,
        ONLINE_CLASS_ADD_PLAYBACK,
        ONLINE_CLASS_ADD_MATERIAL,
        ONLINE_CLASS_GET_TEACHER_ASSIGNMENTS,
        ONLINE_CLASS_DELETE_MATERIAL,
        ONLINE_CLASS_DELETE_PLAYBACK,
        ONLINE_CLASS_GET_COURSE_DISCUSSIONS,
        ONLINE_CLASS_REPLY_TO_DISCUSSION,
        ONLINE_CLASS_PUBLISH_ASSIGNMENT,
        ONLINE_CLASS_GRADE_ASSIGNMENT,
        ONLINE_CLASS_GET_STUDENT_ASSIGNMENTS_FOR_TEACHER,
        ONLINE_CLASS_GET_COURSE_STUDENTS,
        ONLINE_CLASS_GET_TEACHER_COURSES_FOR_ASSIGNMENT,
        ONLINE_CLASS_GET_ASSIGNMENT_DETAILS,
        ONLINE_CLASS_SEARCH_TEACHER_ASSIGNMENTS,
        ONLINE_CLASS_GET_TEACHER_COURSE_NAMES,
        ONLINE_CLASS_SEARCH_TEACHER_COURSES,
        ONLINE_CLASS_GET_CLASS_NAMES,
        ONLINE_CLASS_GET_ASSIGNMENT_FEEDBACK,

//生活服务
        CARD_GET_INFO,
        CARD_GET_CONSUMPTION,
        CARD_GET_RECHARGE,
        CARD_RECHARGE,
        CARD_REPORT_LOSS,
        CARD_UNFREEZE,

        REPAIR_APPLY,
        REPAIR_GET_LIST,
        REPAIR_GET_DETAIL,
        REPAIR_UPDATE_STATUS,
        REPAIR_GET_STATS,

        LIFE_PAYMENT_GET_BILLS,
        LIFE_PAYMENT_PAY,
        LIFE_PAYMENT_GET_RECORDS,

        CARD_ADD,
        REPAIR_GET_ALL,
        REPAIR_ASSIGN_HANDLER,
        LIFE_PAYMENT_ADD_BILL,
        USER_SEARCH,
        CARD_GET_ALL,
        REPAIR_COMPLETE,


        ANNOUNCEMENT_PUBLISH, FILE_DELETE


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



    private String uid; // 唯一标识符

    private Code code; // 状态码

    private String sender; // 发送者

    private long timestamp; // 时间戳

    private String message;



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

        this.success = false; // yhr 9.15 5：30

    }

    public String getReturnType() {

        return returnType;

    }



    public void setReturnType(String returnType) {

        this.returnType = returnType;

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