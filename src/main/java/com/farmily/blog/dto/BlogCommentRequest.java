package com.farmily.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class BlogCommentRequest {
    private Integer blogId;
    private Integer userId;

    @NotBlank(message = "檢舉原因不能空白")
    private String commentPost;

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCommentPost() {
        return commentPost;
    }

    public void setCommentPost(String commentPost) {
        this.commentPost = commentPost;
    }
}
