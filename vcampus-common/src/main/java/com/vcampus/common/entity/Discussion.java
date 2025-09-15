package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Discussion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer discussionId;
    private String courseId;
    private String userId;
    private String userName;
    private String userRole;
    private String content;
    private Timestamp postTime;
    private String parentId;

    public Discussion(Integer discussionId, String courseId, String userId, String userName, String userRole, String content, Timestamp postTime, String parentId) {
        this.discussionId = discussionId;
        this.courseId = courseId;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.content = content;
        this.postTime = postTime;
        this.parentId = parentId;
    }

    public Integer getDiscussionId() { return discussionId; }
    public void setDiscussionId(Integer discussionId) { this.discussionId = discussionId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getPostTime() { return postTime; }
    public void setPostTime(Timestamp postTime) { this.postTime = postTime; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
}
