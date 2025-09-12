package com.vcampus.common.entity;

import java.time.LocalDateTime;

/**
 * 预约实体类
 */
public class Reservation {
    private String id;
    private String spaceId;
    private String userId;
    private String userName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String contact;
    private ReservationStatus status;
    private LocalDateTime createTime;
    private String remark;

    // 预约状态枚举
    public enum ReservationStatus {
        PENDING("待审批"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        CANCELLED("已取消"),
        COMPLETED("已完成");

        private final String displayName;

        ReservationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造函数
    public Reservation() {}

    public Reservation(String id, String spaceId, String userId, String userName,
                       LocalDateTime startTime, LocalDateTime endTime, String purpose,
                       String contact, ReservationStatus status, LocalDateTime createTime,
                       String remark) {
        this.id = id;
        this.spaceId = spaceId;
        this.userId = userId;
        this.userName = userName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.contact = contact;
        this.status = status;
        this.createTime = createTime;
        this.remark = remark;
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", spaceId='" + spaceId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
}