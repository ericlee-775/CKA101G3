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
}
