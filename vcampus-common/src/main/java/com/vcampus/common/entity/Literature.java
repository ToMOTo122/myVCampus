// 文件路径: vcampus-common/src/main/java/com/vcampus/common/entity/Literature.java

package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 文献实体类
 * 用于存储学术文献的基本信息
 */
public class Literature implements Serializable {
    private static final long serialVersionUID = 1L;

    private String literatureId;     // 文献ID
    private String title;            // 标题
    private String author;           // 作者
    private String source;           // 来源/期刊
    private String publishDate;      // 发布日期
    private String link;             // 链接URL
    private String abstractText;     // 摘要
    private String keywords;         // 关键词
    private String category;         // 分类
    private String doi;              // DOI标识
    private int downloadCount;       // 下载次数
    private int citationCount;       // 引用次数
    private String language;         // 语言
    private String fileFormat;       // 文件格式(PDF, HTML等)
    private long fileSize;           // 文件大小(字节)
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间

    // 默认构造函数
    public Literature() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.downloadCount = 0;
        this.citationCount = 0;
    }

    // 基本构造函数
    public Literature(String title, String author, String source, String publishDate, String link) {
        this();
        this.title = title;
        this.author = author;
        this.source = source;
        this.publishDate = publishDate;
        this.link = link;
    }

    // 完整构造函数
    public Literature(String literatureId, String title, String author, String source,
                      String publishDate, String link, String abstractText, String keywords,
                      String category, String doi, String language, String fileFormat, long fileSize) {
        this();
        this.literatureId = literatureId;
        this.title = title;
        this.author = author;
        this.source = source;
        this.publishDate = publishDate;
        this.link = link;
        this.abstractText = abstractText;
        this.keywords = keywords;
        this.category = category;
        this.doi = doi;
        this.language = language;
        this.fileFormat = fileFormat;
        this.fileSize = fileSize;
    }

    // Getter和Setter方法
    public String getLiteratureId() {
        return literatureId;
    }

    public void setLiteratureId(String literatureId) {
        this.literatureId = literatureId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(int citationCount) {
        this.citationCount = citationCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    // 辅助方法

    /**
     * 获取格式化的文件大小字符串
     * @return 格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取短标题（用于列表显示）
     * @param maxLength 最大长度
     * @return 截断后的标题
     */
    public String getShortTitle(int maxLength) {
        if (title == null) return "";
        if (title.length() <= maxLength) {
            return title;
        }
        return title.substring(0, maxLength - 3) + "...";
    }

    /**
     * 获取短摘要（用于预览）
     * @param maxLength 最大长度
     * @return 截断后的摘要
     */
    public String getShortAbstract(int maxLength) {
        if (abstractText == null) return "";
        if (abstractText.length() <= maxLength) {
            return abstractText;
        }
        return abstractText.substring(0, maxLength - 3) + "...";
    }

    /**
     * 检查是否为有效的文献
     * @return 是否有效
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty()
                && author != null && !author.trim().isEmpty()
                && source != null && !source.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Literature{" +
                "literatureId='" + literatureId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", category='" + category + '\'' +
                ", language='" + language + '\'' +
                ", downloadCount=" + downloadCount +
                ", citationCount=" + citationCount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Literature that = (Literature) obj;
        return literatureId != null ? literatureId.equals(that.literatureId) : that.literatureId == null;
    }

    @Override
    public int hashCode() {
        return literatureId != null ? literatureId.hashCode() : 0;
    }
}