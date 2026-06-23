package com.farmily.blog.dao;

import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;

import java.util.List;


public interface BlogDao {

    /* ===== 公開 ===== */

    List<BlogTypeResponse> listTypes() ;

    List<Blog> getAll();

    Blog getBlogById(Integer blogId);

    // 依查詢條件（分類、關鍵字、排序、分頁）撈出文章清單
    List<Blog> getBlogs(BlogQueryParms blogQueryParms);

    // 同樣的查詢條件下，計算總筆數（給分頁用，不含 limit/offset）
    Integer countBlogs(BlogQueryParms blogQueryParms);

    /* ===== 寫作(會員) ===== */

    Integer createBlog(BlogRequest blogRequest);

    void updateBlog(Integer blogId, BlogRequest blogRequest);

    void deleteBlog(Integer blogId);

}
