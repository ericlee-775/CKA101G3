package com.farmily.blog.service;

import com.farmily.blog.dto.*;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;
import com.farmily.blog.model.BlogCommentReport;
import com.farmily.blog.model.BlogReport;
import com.farmily.blog.util.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface BlogService {

    /* ===== 公開 ===== */
    List<BlogTypeResponse> listTypes();

    List<Blog> getAll();

    Page<BlogResponse> getBlogPage(BlogQueryParms parms);

    BlogResponse getBlogDetail(Integer blogId);

    Blog getBlogById(Integer id);

    List<Blog> getBlogs(BlogQueryParms blogQueryParms);

    Integer countBlogs(BlogQueryParms blogQueryParms);

    List<BlogCommentResponse> getBlogComments(Integer blogId);

    List<BlogPhotoResponse> getBlogPhotos(Integer blogId);

    byte[] getPhotoBytes(Integer photoId);

    /* ===== 寫作(會員) ===== */

    BlogResponse createBlog(BlogRequest blogRequest);

    BlogResponse updateBlog(Integer blogId , BlogRequest blogRequest);

    void deleteBlog(Integer blogId);

    List<BlogPhotoResponse> addBlogPhotos(Integer blogId, List<MultipartFile> file) throws IOException;

    void deletePhoto(Integer photoId);

    Page<BlogResponse> getMyBlogs(BlogAuthor author, Integer offset, Integer limit);

    BlogResponse createMyBlog(BlogAuthor author, BlogRequest blogRequest, List<MultipartFile> photos) throws IOException;

    BlogResponse getMyBlog(BlogAuthor author, Integer blogId);

    BlogResponse updateMyBlog(BlogAuthor author, Integer blogId, BlogRequest blogRequest);

    void deleteMyBlog(BlogAuthor author, Integer blogId);

    List<BlogPhotoResponse> addMyBlogPhotos(BlogAuthor author, Integer blogId, List<MultipartFile> files) throws IOException;

    void deleteMyPhoto(BlogAuthor author, Integer photoId);

    /* ===== 互動 ===== */
    // toggle：回傳的 liked=true 代表這次變成已按讚，false=這次取消讚
    BlogResponse likeBlog(Integer blogId, Integer userId);

    // 進頁面時查「這位會員按過沒」；userId 為 null（未登入）時 liked=false
    BlogResponse getLikeStatus(Integer blogId, Integer userId);

    BlogCommentResponse addBlogComment(BlogCommentRequest blogComment);

    BlogComment getBlogCommentsById(Integer commentId);

    void deleteComment(Integer commentId, Integer userId);

    Integer reportBlog(Integer blogId ,BlogReportRequest blogReportRequest);

    BlogReport getBlogReportById(Integer blogReportId);

    Integer reportComment(Integer commentId, BlogReportRequest request);

    BlogCommentReport getCommentReportById(Integer reportCommentId);




}
