package com.farmily.blog.service;

import com.farmily.blog.dto.BlogCommentRequest;
import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;

import java.util.List;


public interface BlogService {

    /* ===== 公開 ===== */
    List<BlogTypeResponse> listTypes();

    List<Blog> getAll();

    Blog getBlogById(Integer id);

    List<Blog> getBlogs(BlogQueryParms blogQueryParms);

    Integer countBlogs(BlogQueryParms blogQueryParms);

    List<BlogComment> getBlogComments(Integer blogId);

    /* ===== 寫作(會員) ===== */

    Integer createBlog(BlogRequest blogRequest);

    void updateBlog(Integer blogId , BlogRequest blogRequest);

    void deleteBlog(Integer blogId);

    /* ===== 互動 ===== */
    // toggle：回傳 true=這次變成已按讚，false=這次取消讚
    boolean likeBlog(Integer blogId, Integer userId);

    Integer addBlogComment(BlogCommentRequest blogComment);

    BlogComment getBlogCommentsById(Integer commentId);


    void deleteComment(Integer commentId);




}
