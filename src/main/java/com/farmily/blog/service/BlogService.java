package com.farmily.blog.service;

import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BlogService {

    /* ===== 公開 ===== */
    List<BlogTypeResponse> listTypes();

    List<Blog> getAll();

    Blog getBlogById(Integer id);

    List<Blog> getBlogs(BlogQueryParms blogQueryParms);

    Integer countBlogs(BlogQueryParms blogQueryParms);

    /* ===== 寫作(會員) ===== */

    Integer createBlog(BlogRequest blogRequest);

    void updateBlog(Integer blogId , BlogRequest blogRequest);

    void deleteBlog(Integer blogid);


}
