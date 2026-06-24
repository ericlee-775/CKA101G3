package com.farmily.blog.dao;

import com.farmily.blog.dto.BlogCommentRequest;
import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;
import com.farmily.blog.model.BlogPhoto;

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

    List<BlogComment> getBlogComments(Integer blogId);

    List<BlogPhoto> getBlogPhotos(Integer blogId);

    byte[] getPhotoBytes(Integer photoId);

    /* ===== 寫作(會員) ===== */

    Integer createBlog(BlogRequest blogRequest);

    void updateBlog(Integer blogId, BlogRequest blogRequest);

    void deleteBlog(Integer blogId);

    void addBlogPhotos(Integer blogId, List<byte[]> photoList);  // 批次新增

    void deletePhoto(Integer photoId);

    /* ===== 互動 ===== */

    // 這個會員是否已經按過這篇
    boolean existsLike(Integer blogId, Integer userId);

    // 新增一筆按讚紀錄
    void insertLike(Integer blogId, Integer userId);

    // 移除這個會員對這篇的按讚紀錄
    void deleteLike(Integer blogId, Integer userId);

    // blog 表的讚數 +1 / -1
    void increaseLikeCount(Integer blogId);

    void decreaseLikeCount(Integer blogId);

    Integer addComment(BlogCommentRequest blogComment);

    BlogComment getBlogCommentById(Integer commentId);

    void delteComment(Integer commentId);

}
