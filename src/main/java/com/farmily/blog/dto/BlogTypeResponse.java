package com.farmily.blog.dto;

import com.farmily.blog.model.BlogType;

public class BlogTypeResponse {
    private Integer blogTypeId;
    private String blogTypeName;
    private String blogTypeText;
    private byte[] blogTypeImg;
    private Integer sortOrder;


    public Integer getBlogTypeId() {
        return blogTypeId;
    }

    public void setBlogTypeId(Integer blogTypeId) {
        this.blogTypeId = blogTypeId;
    }

    public String getBlogTypeName() {
        return blogTypeName;
    }

    public void setBlogTypeName(String blogTypeName) {
        this.blogTypeName = blogTypeName;
    }

    public String getBlogTypeText() {
        return blogTypeText;
    }

    public void setBlogTypeText(String blogTypeText) {
        this.blogTypeText = blogTypeText;
    }

    public byte[] getBlogTypeImg() {
        return blogTypeImg;
    }

    public void setBlogTypeImg(byte[] blogTypeImg) {
        this.blogTypeImg = blogTypeImg;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }



    public static BlogTypeResponse from(BlogType t) { //轉型
        BlogTypeResponse r = new BlogTypeResponse();
        r.setBlogTypeId(t.getBlogTypeId());
        r.setBlogTypeName(t.getBlogTypeName());
        r.setBlogTypeText(t.getBlogTypeText());
        r.setBlogTypeImg(t.getBlogTypeImg());

        return r;
    }


}
