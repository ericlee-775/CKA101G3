package com.farmily.blog.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class BlogPhotoRequest {
    private Integer blogId;
    private byte[] blogPhoto;

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public byte[] getBlogPhoto() {
        return blogPhoto;
    }

    public void setBlogPhoto(byte[] blogPhoto) {
        this.blogPhoto = blogPhoto;
    }

    public void setBlogPhoto(MultipartFile multipartFile) {
        try {
            this.blogPhoto = multipartFile.getBytes();   // 檔案 → byte[]
        } catch (IOException e) {
            throw new RuntimeException("圖片讀取失敗", e);
        }
    }
}
