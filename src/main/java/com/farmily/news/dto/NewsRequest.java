package com.farmily.news.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;

public class NewsRequest {

    @NotBlank
    @Size(max = 100)
    private String title;
    @NotBlank
    private String content;

    private byte[] coverImg;

    private Timestamp publishTime;

    // 前端 multipart 的檔案會進到這個 setter，Spring 自動呼叫
    public void setCoverImg(MultipartFile multipartFile) {
        try {
            this.coverImg = multipartFile.getBytes();   // 檔案 → byte[]
        } catch (IOException e) {
            throw new RuntimeException("圖片讀取失敗", e);
        }
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
}
