package com.farmily.blog.dto;

//把文章檢舉和留言檢舉統一成一個格式回傳給前端

import com.farmily.blog.constant.BlogReportStatus;

import java.sql.Timestamp;

public class BlogAdminReport {

    private Integer reportId;
    private String targetType;
    private Integer blogId;
    private Integer commentId;
    private String reportReason;
    private BlogReportStatus reportStatus;
    private Timestamp reportTime;

    // 補進來給後台審核用的顯示欄位（來自 Blog / BlogComment，非檢舉表本身）
    private String blogTitle;      // 文章標題（取代原本只顯示 blogId）
    private String blogContent;    // 文章內容（審核時看全文）
    private String commentContent; // 留言內容（留言檢舉審核用）

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public BlogReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(BlogReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public Timestamp getReportTime() {
        return reportTime;
    }

    public void setReportTime(Timestamp reportTime) {
        this.reportTime = reportTime;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public String getBlogContent() {
        return blogContent;
    }

    public void setBlogContent(String blogContent) {
        this.blogContent = blogContent;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }
}
