package com.farmily.blog.service.impl;

import com.farmily.blog.dao.BlogDao;
import com.farmily.blog.dto.*;
import com.farmily.blog.model.*;
import com.farmily.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.farmily.blog.util.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
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
    public Page<BlogResponse> getBlogPage(BlogQueryParms parms) {
        List<Blog> blogList = blogDao.getBlogs(parms);      // dao 撈這頁的 entity
        Integer total = blogDao.countBlogs(parms);          // dao 算總數

        List<BlogResponse> results = new ArrayList<>();     // entity → DTO
        for (Blog b : blogList) {
            results.add(BlogResponse.from(b));
        }

        Page<BlogResponse> page = new Page<>();             // 組分頁物件
        page.setLimit(parms.getLimit());
        page.setOffset(parms.getOffset());
        page.setTotal(total);
        page.setResults(results);
        return page;
    }

    @Override
    public BlogResponse getBlogDetail(Integer blogId) {
       Blog blog = blogDao.getBlogById(blogId); //dao 撈entity
       if(blog != null) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文張不存在：" + blogId);
       }
        return BlogResponse.from(blog); //轉成 DTO回去
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

    @Override
    public List<BlogComment> getBlogComments(Integer blogId) {
        return blogDao.getBlogComments(blogId);
    }

    @Override
    public List<BlogPhotoResponse> getBlogPhotos(Integer blogId) {
        List<BlogPhoto> photos = blogDao.getBlogPhotos(blogId);

        List<BlogPhotoResponse> result = new ArrayList<>();        // 空清單，準備裝 DTO
        for (BlogPhoto photo : photos) {                          // 每一張 photo
            result.add(BlogPhotoResponse.from(photo));           // 翻成 DTO，放進清單
        }
        return result;
    }

    @Override
    public byte[] getPhotoBytes(Integer photoId) {
        return blogDao.getPhotoBytes(photoId);
    }

    /* ===== 寫作(會員) ===== */
    private static final Integer FARMBLOGTYPEID = 1;

    @Override
    public BlogResponse createBlog(BlogRequest blogRequest) {

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

        Integer blogId = blogDao.createBlog(blogRequest);   // dao 建立，回 id
        Blog blog = blogDao.getBlogById(blogId);            // 撈出剛建立的 entity
        return BlogResponse.from(blog);
    }

    @Override
    public BlogResponse updateBlog(Integer blogId, BlogRequest blogRequest) {

        Blog blog = blogDao.getBlogById(blogId);
        if (blog == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在: " + blogId);
        }
        // 前端沒選新圖 → blogImg 是 null → 沿用 DB 裡的舊圖
        if (blogRequest.getBlogImg() == null) {
            blogRequest.setBlogImg(blog.getBlogImg());
        }
        blogDao.updateBlog(blogId, blogRequest);
        return BlogResponse.from(blogDao.getBlogById(blogId));   // 回更新後的圖
    }

    @Override
    @Transactional
    public void deleteBlog(Integer blogId) {
        blogDao.deleteCommentReportsByBlogId(blogId);  //先刪「留言的檢舉」
        blogDao.deleteCommentsByBlogId(blogId);        //再刪留言本身
        blogDao.deleteReportsByBlogId(blogId);         //刪文章的檢舉
        blogDao.deletePhotosByBlogId(blogId);          //刪照片
        blogDao.deleteLikesByBlogId(blogId);           //刪讚
        blogDao.deleteBlog(blogId);

    }

    @Override
    public List<BlogPhotoResponse> addBlogPhotos(Integer blogId, List<MultipartFile> files) throws IOException {
        //文章存在檢查
        Blog blog = blogDao.getBlogById(blogId);
        if (blog == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在: " + blogId);
        }
        // 2. 逐檔驗證，並轉成 byte[]
        List<byte[]> photoList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;   // 空檔跳過
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "只能上傳圖片");
            }
            photoList.add(file.getBytes());
        }
        if (photoList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "沒有有效的圖片檔");
        }

        //存進資料庫
        blogDao.addBlogPhotos(blogId, photoList);

        //回傳DTO
        return getBlogPhotos(blogId);

    }

    @Override
    public void deletePhoto(Integer photoId) {
        blogDao.deletePhoto(photoId);

    }

    /* ===== 互動 ===== */

    @Override
    @Transactional
    public BlogResponse likeBlog(Integer blogId, Integer userId) {
        Blog blog = blogDao.getBlogById(blogId);
        if (blog == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在: " + blogId);
        }
        if (blogDao.existsLike(blogId, userId)) {
            // 已經按過 → 取消讚
            blogDao.deleteLike(blogId, userId);
            blogDao.decreaseLikeCount(blogId);
        } else {
            // 還沒按過 → 按讚
            blogDao.insertLike(blogId, userId);
            blogDao.increaseLikeCount(blogId);
        }
        return BlogResponse.from(blogDao.getBlogById(blogId));
    }

    @Override
    public Integer addBlogComment(BlogCommentRequest blogComment) {
        Integer userId = blogComment.getUserId();
        Integer blogId = blogComment.getBlogId();

        if(userId == null ||     blogId == null) {
            throw new IllegalArgumentException("使用者和文章不能為NULL");
        }


        return blogDao.addComment(blogComment);
    }

    @Override
    public BlogComment getBlogCommentsById(Integer commentId) {
        return blogDao.getBlogCommentById(commentId);
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId) {
        blogDao.deleteCommentReportsByCommentId(commentId);  // 先刪這則留言的檢舉
        blogDao.delteComment(commentId);

    }

    @Override
    public Integer reportBlog(Integer blogId ,BlogReportRequest blogReportRequest) {
        Integer userId = blogReportRequest.getUserId();


        if(userId == null || blogId == null) {
            throw new IllegalArgumentException("使用者和文章不能為 NULL");

        }

        return blogDao.reportBlog(blogId , blogReportRequest);

    }

    @Override
    public BlogReport getBlogReportById(Integer blogReportId) {

        return blogDao.getBlogReportById(blogReportId);
    }

    @Override
    public Integer reportComment(Integer commentId, BlogReportRequest request) {
        Integer userId = request.getUserId();
        if(userId == null || commentId == null) {
            throw new IllegalArgumentException("使用者和留言不能為 NULL");
        }
        BlogComment blogComment = blogDao.getBlogCommentById(commentId);
        if(blogComment == null) {
            throw new IllegalArgumentException("留言不存在");
        }
        return blogDao.reportComment(commentId, blogComment.getBlogId(), request);
    }

    @Override
    public BlogCommentReport getCommentReportById(Integer reportCommentId) {
        return blogDao.getCommentReportById(reportCommentId);
    }
}
