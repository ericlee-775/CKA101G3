package com.farmily.blog.dto;

public class BlogRequest {

    private String blogTitle;
    private Integer userId;
    private Integer farmerId;
    private Integer blogTypeId;
    private Integer productId;
    private String blogContent;
    private byte[] blogImg;

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

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getBlogContent() {
        return blogContent;
    }

    public void setBlogContent(String blogContent) {
        this.blogContent = blogContent;
    }

    public byte[] getBlogImg() {
        return blogImg;
    }

    public void setBlogImg(byte[] blogImg) {
        this.blogImg = blogImg;
    }
}
