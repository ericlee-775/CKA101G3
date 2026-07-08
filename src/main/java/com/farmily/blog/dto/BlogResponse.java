package com.farmily.blog.dto;

import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.model.Blog;

import java.sql.Timestamp;

public class BlogResponse {
    private Integer blogId;
    private String blogTitle;
    private Integer userId;
    private Integer farmerId;
    private Integer blogTypeId;
    private String blogContent;
    private Integer blogLikeCount;
    private Timestamp blogTime;
    private BlogStatus blogStatus;
    //private byte[] blogImg; 圖片走 /api/blogs/{id}/image

    //entity轉DTO
    public static BlogResponse from(Blog b) {
        BlogResponse r = new BlogResponse();
        r.setBlogId(b.getBlogId());
        r.setBlogTitle(b.getBlogTitle());
        r.setUserId(b.getUserId());
        r.setFarmerId(b.getFarmerId());
        r.setBlogTypeId(b.getBlogTypeId());
        r.setBlogContent(b.getBlogContent());
        r.setBlogLikeCount(b.getBlogLikeCount());
        r.setBlogTime(b.getBlogTime());
        r.setBlogStatus(b.getBlogStatus());
        return r;
    }


    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Integer farmerId) {
        this.farmerId = farmerId;
    }

    public Integer getBlogTypeId() {
        return blogTypeId;
    }

    public void setBlogTypeId(Integer blogTypeId) {
        this.blogTypeId = blogTypeId;
    }

    public String getBlogContent() {
        return blogContent;
    }

    public void setBlogContent(String blogContent) {
        this.blogContent = blogContent;
    }

    public Integer getBlogLikeCount() {
        return blogLikeCount;
    }

    public void setBlogLikeCount(Integer blogLikeCount) {
        this.blogLikeCount = blogLikeCount;
    }

    public Timestamp getBlogTime() {
        return blogTime;
    }

    public void setBlogTime(Timestamp blogTime) {
        this.blogTime = blogTime;
    }

    public BlogStatus getBlogStatus() {
        return blogStatus;
    }

    public void setBlogStatus(BlogStatus blogStatus) {
        this.blogStatus = blogStatus;
    }
}
