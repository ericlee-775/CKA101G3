package com.farmily.news.dto;

import com.farmily.news.entity.NewStatus;
import com.farmily.news.entity.News;

import java.sql.Timestamp;

public class NewsResponse {
    private Integer newsId;
    private Integer adminId;
    private String title;
    private String content;
    //圖片走/api/news/{newsId}/image
    private Timestamp publishTime;
    private NewStatus newsStatus;
    private Timestamp createdAt;

    public static NewsResponse from(News news) {

        NewsResponse response = new NewsResponse();

        response.newsId = news.getNewId();
        response.adminId = news.getAdminId();
        response.title = news.getTitle();
        response.content = news.getContent();
        response.publishTime = news.getPublishTime();
        response.newsStatus = news.getNewsStatus();
        response.createdAt = news.getCreatedAt();

        return response;
    }

    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Timestamp publishTime) {
        this.publishTime = publishTime;
    }

    public NewStatus getNewsStatus() {
        return newsStatus;
    }

    public void setNewsStatus(NewStatus newsStatus) {
        this.newsStatus = newsStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
