package com.farmily.blog.dto;

import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.model.BlogComment;

import java.sql.Timestamp;

public class BlogCommentResponse {

    private Integer commentId;
    private Integer blogId;
    private Integer userId;
    private Timestamp commentTime;
    private String commentPost;
    private Integer commentLike;
    private BlogStatus commentStatus;

    public static BlogCommentResponse from(BlogComment c) {
        BlogCommentResponse r = new BlogCommentResponse();
        r.setCommentId(c.getCommentId());
        r.setBlogId(c.getBlogId());
        r.setUserId(c.getUserId());
        r.setCommentTime(c.getCommentTime());
        r.setCommentPost(c.getCommentPost());
        r.setCommentLike(c.getCommentLike());
        r.setCommentStatus(c.getCommentStatus());
        return r;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

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

    public Timestamp getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Timestamp commentTime) {
        this.commentTime = commentTime;
    }

    public String getCommentPost() {
        return commentPost;
    }

    public void setCommentPost(String commentPost) {
        this.commentPost = commentPost;
    }

    public Integer getCommentLike() {
        return commentLike;
    }

    public void setCommentLike(Integer commentLike) {
        this.commentLike = commentLike;
    }

    public BlogStatus getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(BlogStatus commentStatus) {
        this.commentStatus = commentStatus;
    }
}
