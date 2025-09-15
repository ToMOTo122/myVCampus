// 文件路径: vcampus-common/src/main/java/com/vcampus/common/entity/Activity.java

package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 图书馆活动实体类
 * 用于存储图书馆举办的各类活动信息
 */
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String activityId;       // 活动ID
    private String title;            // 活动标题
    private String description;      // 活动描述
    private String content;          // 活动详细内容
    private String category;         // 活动类别（讲座、展览、培训等）
    private String location;         // 活动地点
    private Date startTime;          // 开始时间
    private Date endTime;            // 结束时间
    private String organizer;        // 主办方
    private String speaker;          // 演讲者/主讲人
    private int maxParticipants;     // 最大参与人数
    private int currentParticipants; // 当前参与人数
    private String status;           // 活动状态（报名中、进行中、已结束、已取消）
    private String imageUrl;         // 活动图片URL
    private String registrationUrl;  // 报名链接
    private boolean requiresRegistration; // 是否需要报名
    private String contactInfo;      // 联系方式
    private String requirements;     // 参与要求
    private String benefits;         // 活动收益
    private String tags;             // 标签（用逗号分隔）
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间
    private String createdBy;        // 创建者

    // 活动状态常量
    public static final String STATUS_REGISTRATION = "报名中";
    public static final String STATUS_ONGOING = "进行中";
    public static final String STATUS_FINISHED = "已结束";
    public static final String STATUS_CANCELLED = "已取消";
    public static final String STATUS_FULL = "已满员";

    // 活动类别常量
    public static final String CATEGORY_LECTURE = "学术讲座";
    public static final String CATEGORY_EXHIBITION = "图书展览";
    public static final String CATEGORY_TRAINING = "技能培训";
    public static final String CATEGORY_READING = "读书活动";
    public static final String CATEGORY_WORKSHOP = "工作坊";
    public static final String CATEGORY_COMPETITION = "知识竞赛";

    // 默认构造函数
    public Activity() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.currentParticipants = 0;
        this.requiresRegistration = false;
        this.status = STATUS_REGISTRATION;
    }

    // 基本构造函数
    public Activity(String title, String description, String location, Date startTime, Date endTime) {
        this();
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 完整构造函数
    public Activity(String activityId, String title, String description, String content,
                    String category, String location, Date startTime, Date endTime,
                    String organizer, String speaker, int maxParticipants, String status) {
        this();
        this.activityId = activityId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.category = category;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
        this.speaker = speaker;
        this.maxParticipants = maxParticipants;
        this.status = status;
    }

    // Getter和Setter方法
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }

    public boolean isRequiresRegistration() {
        return requiresRegistration;
    }

    public void setRequiresRegistration(boolean requiresRegistration) {
        this.requiresRegistration = requiresRegistration;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // 辅助方法

    /**
     * 获取格式化的开始时间字符串
     * @return 格式化的时间字符串
     */
    public String getFormattedStartTime() {
        if (startTime == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(startTime);
    }

    /**
     * 获取格式化的结束时间字符串
     * @return 格式化的时间字符串
     */
    public String getFormattedEndTime() {
        if (endTime == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(endTime);
    }

    /**
     * 获取活动时间段字符串
     * @return 时间段字符串
     */
    public String getTimeRange() {
        return getFormattedStartTime() + " - " + getFormattedEndTime();
    }

    /**
     * 检查活动是否已满员
     * @return 是否已满员
     */
    public boolean isFull() {
        return maxParticipants > 0 && currentParticipants >= maxParticipants;
    }

    /**
     * 检查活动是否可以报名
     * @return 是否可以报名
     */
    public boolean canRegister() {
        return STATUS_REGISTRATION.equals(status) && !isFull();
    }

    /**
     * 检查活动是否已开始
     * @return 是否已开始
     */
    public boolean hasStarted() {
        return startTime != null && new Date().after(startTime);
    }

    /**
     * 检查活动是否已结束
     * @return 是否已结束
     */
    public boolean hasEnded() {
        return endTime != null && new Date().after(endTime);
    }

    /**
     * 获取剩余名额
     * @return 剩余名额
     */
    public int getRemainingSlots() {
        if (maxParticipants <= 0) return Integer.MAX_VALUE;
        return Math.max(0, maxParticipants - currentParticipants);
    }

    /**
     * 获取参与率百分比
     * @return 参与率
     */
    public double getParticipationRate() {
        if (maxParticipants <= 0) return 0.0;
        return (double) currentParticipants / maxParticipants * 100;
    }

    /**
     * 获取短描述（用于列表显示）
     * @param maxLength 最大长度
     * @return 截断后的描述
     */
    public String getShortDescription(int maxLength) {
        if (description == null) return "";
        if (description.length() <= maxLength) {
            return description;
        }
        return description.substring(0, maxLength - 3) + "...";
    }

    /**
     * 检查活动信息是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty()
                && location != null && !location.trim().isEmpty()
                && startTime != null && endTime != null
                && startTime.before(endTime);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "activityId='" + activityId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", startTime=" + getFormattedStartTime() +
                ", endTime=" + getFormattedEndTime() +
                ", status='" + status + '\'' +
                ", participants=" + currentParticipants + "/" + maxParticipants +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Activity activity = (Activity) obj;
        return activityId != null ? activityId.equals(activity.activityId) : activity.activityId == null;
    }

    @Override
    public int hashCode() {
        return activityId != null ? activityId.hashCode() : 0;
    }
}