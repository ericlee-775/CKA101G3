package com.farmily.news.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "NEWS")
public class News implements java.io.Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "new_id", updatable = false)
    private Integer newId;

    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Lob
    @Column(name = "cover_img")
    private byte[] coverImg;

    @Column(name = "publish_time")
    private Timestamp publishTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_status")
    private NewStatus newsStatus;

    @CreationTimestamp  //自動塞時間
    @Column(name = "created_at")
    private Timestamp createdAt;

    public Integer getNewId() {
        return newId;
    }

    public void setNewId(Integer newId) {
        this.newId = newId;
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

    public byte[] getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(byte[] coverImg) {
        this.coverImg = coverImg;
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
