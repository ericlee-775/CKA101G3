package com.farmily.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class BlogRequest {

    @NotBlank
    private String blogTitle;
    private Integer userId;
    private Integer farmerId;
    @NotNull
    private Integer blogTypeId;
    private Integer productId;
    @NotBlank
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

    // 前端 multipart 的檔案會進到這個 setter，Spring 自動呼叫
    public void setBlogImg(MultipartFile multipartFile) {
        try {
            this.blogImg = multipartFile.getBytes();   // 檔案 → byte[]
        } catch (IOException e) {
            throw new RuntimeException("圖片讀取失敗", e);
        }
    }
}
