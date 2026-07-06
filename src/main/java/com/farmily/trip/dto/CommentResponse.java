package com.farmily.trip.dto;

import java.sql.Timestamp;

// 回傳給前端看的留言資料
public class CommentResponse {

    private Integer commentId;
    private Integer farmTripId;
    private Integer userId;
    private Integer star;
    private String content;
    private Timestamp createdAt;

    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }

    public Integer getFarmTripId() { return farmTripId; }
    public void setFarmTripId(Integer farmTripId) { this.farmTripId = farmTripId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getStar() { return star; }
    public void setStar(Integer star) { this.star = star; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
