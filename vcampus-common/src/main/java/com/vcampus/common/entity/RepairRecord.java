package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class RepairRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int repairId;
    private String userId;
    private String title;
    private String description;
    private String location;
    private String priority;
    private String status;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String handler;
    private Timestamp handleTime;
    private String remark;

    // Getter和Setter方法
    public int getRepairId() { return repairId; }
    public void setRepairId(int repairId) { this.repairId = repairId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    public Timestamp getHandleTime() { return handleTime; }
    public void setHandleTime(Timestamp handleTime) { this.handleTime = handleTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
