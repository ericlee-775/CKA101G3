package com.farmily.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class BlogReportRequest {

    @NotBlank(message = "檢舉原因不能空白")
    private String reportReason;
    private Integer userId;

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
