package com.farmily.trip.dto;

// 消費者留言/評分時送進來的資料(留哪個活動由網址決定)
public class CommentCreateRequest {

    private Integer userId;   // 之後接登入功能改從 token 拿，先由前端送
    private Integer star;     // 評分 1~5
    private String content;  // 留言內容

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getStar() { return star; }
    public void setStar(Integer star) { this.star = star; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
