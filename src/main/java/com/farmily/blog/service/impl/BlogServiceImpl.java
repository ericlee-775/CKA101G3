package com.farmily.blog.service.impl;

import com.farmily.blog.dao.BlogDao;
import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogDao blogDao;

    /* ===== 公開 ===== */
    @Override
    public List<BlogTypeResponse> listTypes() {
        return blogDao.listTypes();

    }

    @Override
    public List<Blog> getAll() {
        return blogDao.getAll();
    }

    @Override
    public Blog getBlogById(Integer blogId) {
        return blogDao.getBlogById(blogId);
    }

    @Override
    public List<Blog> getBlogs(BlogQueryParms blogQueryParms) {
        return blogDao.getBlogs(blogQueryParms);
    }

    @Override
    public Integer countBlogs(BlogQueryParms blogQueryParms) {
        return blogDao.countBlogs(blogQueryParms);
    }

    /* ===== 寫作(會員) ===== */

    @Override
    public Integer createBlog(BlogRequest blogRequest) {
        return blogDao.createBlog(blogRequest);
    }

    @Override
    public void updateBlog(Integer blogId, BlogRequest blogRequest) {
        blogDao.updateBlog(blogId, blogRequest);
    }

    @Override
    public void deleteBlog(Integer blogId) {
        blogDao.deleteBlog(blogId);

    }
}
