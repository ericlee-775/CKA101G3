package com.farmily.blog.service;

import com.farmily.blog.dto.*;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;
import com.farmily.blog.model.BlogPhoto;
import com.farmily.blog.model.BlogReport;

import java.util.List;


public interface BlogService {

    /* ===== 公開 ===== */
    List<BlogTypeResponse> listTypes();

    List<Blog> getAll();

    Blog getBlogById(Integer id);

    List<Blog> getBlogs(BlogQueryParms blogQueryParms);

    Integer countBlogs(BlogQueryParms blogQueryParms);

    List<BlogComment> getBlogComments(Integer blogId);

    List<BlogPhoto> getBlogPhotos(Integer blogId);

    byte[] getPhotoBytes(Integer photoId);

    /* ===== 寫作(會員) ===== */

    Integer createBlog(BlogRequest blogRequest);

    void updateBlog(Integer blogId , BlogRequest blogRequest);

    void deleteBlog(Integer blogId);

    void addBlogPhotos(Integer blogId, List<byte[]> photoList);

    void deletePhoto(Integer photoId);

    /* ===== 互動 ===== */
    // toggle：回傳 true=這次變成已按讚，false=這次取消讚
    boolean likeBlog(Integer blogId, Integer userId);

    Integer addBlogComment(BlogCommentRequest blogComment);

    BlogComment getBlogCommentsById(Integer commentId);

    void deleteComment(Integer commentId);

    Integer reportBlog(Integer blogId ,BlogReportRequest blogReportRequest);

    BlogReport getBlogReportById(Integer blogReportId);




}
