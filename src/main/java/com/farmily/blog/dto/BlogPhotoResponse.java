package com.farmily.blog.dto;

import com.farmily.blog.model.BlogPhoto;

//這是從資料庫拿圖傳給前端，controller~前端
public class BlogPhotoResponse {

    private Integer blogPhotoId;
    private Integer blogId;
    // 注意：前端是用 /photos/{id}/image 那支 API 去拿圖，
    //       列表只需要 id，不需要把整包二進位塞進 JSON。

    // 把 entity 轉成這個 DTO 的方法
    public static BlogPhotoResponse from(BlogPhoto p) {
        BlogPhotoResponse r = new BlogPhotoResponse();
        r.setBlogPhotoId(p.getBlogPhotoId());
        r.setBlogId(p.getBlogId());
        return r;
    }

    public Integer getBlogPhotoId() {
        return blogPhotoId;
    }

    public void setBlogPhotoId(Integer blogPhotoId) {
        this.blogPhotoId = blogPhotoId;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }
}
