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
    private static final Integer FARMBLOGTYPEID = 1;

    @Override
    public Integer createBlog(BlogRequest blogRequest) {

        Integer farmerId   = blogRequest.getFarmerId();
        Integer userId     = blogRequest.getUserId();
        Integer blogTypeId = blogRequest.getBlogTypeId();

        if(userId != null && farmerId != null) {
            throw new IllegalArgumentException("一篇文章只有一個作者");
        }
        if(userId == null && farmerId == null) {
            throw new IllegalArgumentException("必須要有作者");
        }

        if(farmerId != null) {
            //小農：只能發表是 產地日記
            if(!FARMBLOGTYPEID.equals(blogTypeId)) {
                throw new IllegalArgumentException("小農只能發表產地日記");
            }

        }else {
            //會員：不能關聯商品
            if(blogRequest.getProductId() != null) {
                throw new IllegalArgumentException("只有小農可以關聯商品");
            }
            //會員：類別不能選 產地日記
            if(FARMBLOGTYPEID.equals(blogTypeId)) {
                throw new IllegalArgumentException("會員不可發表產地日記");
            }
        }

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
