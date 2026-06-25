package com.farmily.blog.controller;

import com.farmily.blog.dto.*;
import com.farmily.blog.model.*;
import com.farmily.blog.service.BlogService;
import com.farmily.blog.util.Page;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /* ===== 公開 ===== */

    @GetMapping("/blogs/types")
    public List<BlogTypeResponse> types() {return blogService.listTypes();}

    @GetMapping("/blogs")
    public ResponseEntity<Page<Blog>> getBlogAll(
            //查詢條件
            @RequestParam(required = false) Integer blogTypeId,
            @RequestParam(required = false) String search,
            //排序
            @RequestParam(defaultValue = "blog_time") String orderBy,
            @RequestParam(defaultValue = "desc") String sort,
            //分頁
            @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        BlogQueryParms blogQueryParms = new BlogQueryParms();
        blogQueryParms.setBlogTypeId(blogTypeId);
        blogQueryParms.setSearch(search);
        blogQueryParms.setOrderBy(orderBy);
        blogQueryParms.setSort(sort);
        blogQueryParms.setLimit(limit);
        blogQueryParms.setOffset(offset);

        List<Blog> blogList = blogService.getBlogs(blogQueryParms);

        Integer total = blogService.countBlogs(blogQueryParms);

        Page<Blog> page = new Page<>();
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal(total);
        page.setResults(blogList);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @GetMapping("/blogs/{blogId}")
    public ResponseEntity<Blog> getBlogOne(@PathVariable Integer blogId) {
        //從service 去抓blogId
        Blog blog = blogService.getBlogById(blogId);

        if (blog != null) {
            return ResponseEntity.status(HttpStatus.OK).body(blog);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @GetMapping("/blogs/{blogId}/comments")
    public ResponseEntity<List<BlogComment>> getBlogComments( @PathVariable Integer blogId)  {

        List<BlogComment> comments = blogService.getBlogComments(blogId);

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/blogs/{blogId}/photos")
    public ResponseEntity<List<BlogPhoto>> getBlogPhotos(@PathVariable Integer blogId) {

        List<BlogPhoto> blogPhotos = blogService.getBlogPhotos(blogId);

        return ResponseEntity.ok(blogPhotos);
    }

    @GetMapping("/photos/{photoId}/image")
    public void getPhotoImg(HttpServletResponse res, @PathVariable Integer photoId)
            throws IOException {

        byte[] img = blogService.getPhotoBytes(photoId);
        ServletOutputStream out = res.getOutputStream();

        if (img != null && img.length > 0) {
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
            res.setContentType(type != null ? type : MediaType.IMAGE_JPEG_VALUE);
            out.write(img);
        } else {
            res.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }


    /* ===== 寫作(會員) ===== */

    @PostMapping(value = "/blogs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Blog> createBlog(@ModelAttribute @Valid BlogRequest blogRequest) {

       Integer BlogId = blogService.createBlog(blogRequest);

        Blog newBlog = blogService.getBlogById(BlogId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newBlog);


    }

    @PutMapping(value ="/blogs/{blogId}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Blog> updateBlog(@PathVariable Integer blogId, @ModelAttribute @Valid BlogRequest blogRequest) {
       Blog blog = blogService.getBlogById(blogId);

       if(blog == null) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       }
        // 前端沒選新檔 → blogImg 會是 null → 沿用 DB 裡的舊圖
        if(blogRequest.getBlogImg() == null) {
            blogRequest.setBlogImg(blog.getBlogImg());
        }

       blogService.updateBlog(blogId, blogRequest);

        Blog updateBlog = blogService.getBlogById(blogId);

        return ResponseEntity.status(HttpStatus.OK).body(updateBlog);

    }

    @DeleteMapping("blogs/{blogId}")
    public ResponseEntity<?> deleteBlog(@PathVariable Integer blogId) {
        blogService.deleteBlog(blogId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); //告訴前端刪除的東西已經不存在


    }
    //讀取圖片
    @GetMapping("/blogs/{blogId}/image")
    public void getHandleImg(HttpServletResponse res, @PathVariable Integer blogId) throws IOException {

        Blog blog = blogService.getBlogById(blogId);
        ServletOutputStream out= res.getOutputStream();

        if(blog != null && blog.getBlogImg() != null && blog.getBlogImg().length > 0) {
           byte[] img = blog.getBlogImg();
            // 自動判斷是 jpg/png/gif，判不出就當 jpeg
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
            res.setContentType(type != null ? type : MediaType.IMAGE_JPEG_VALUE);
            out.write(img);
        } else {
            res.setStatus(HttpStatus.NOT_FOUND.value()); //沒圖 404 前端@error 會自動隱藏
        }

    }

    // 上傳照片集 (批次上傳多張)
    @PostMapping("/blogs/{blogId}/photos")
    public ResponseEntity<List<BlogPhoto>> uploadPhotos(
            @PathVariable Integer blogId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        List<byte[]> photoList = new ArrayList<>();
        for (MultipartFile file : files) {
            photoList.add(file.getBytes());   // MultipartFile → byte[]
        }
        blogService.addBlogPhotos(blogId, photoList);

        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.getBlogPhotos(blogId));
    }



    // 刪除照片
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Integer photoId) {
        blogService.deletePhoto(photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }




    /* ===== 互動 ===== */
    @PostMapping("/blogs/{blogId}/like")
    public ResponseEntity<Blog> likeBlog(@PathVariable Integer blogId,
                                         @RequestParam Integer userId) {
        // TODO: 之後登入做好，userId 改成從 token 解析，不再由前端傳
        Blog blog = blogService.getBlogById(blogId);

        if (blog == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        blogService.likeBlog(blogId, userId);

        return ResponseEntity.ok(blogService.getBlogById(blogId));
    }


    @PostMapping("/blogs/{blogId}/comments")
    public ResponseEntity<BlogComment> addBlogComment(@PathVariable Integer blogId, @RequestBody BlogCommentRequest request) {
        request.setBlogId(blogId);

        Integer commentId = blogService.addBlogComment(request);

        BlogComment newComment = blogService.getBlogCommentsById(commentId);
        //201
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @DeleteMapping("/blogs/comments/{commentId}")
    public ResponseEntity<?> deleteComment (@PathVariable Integer commentId) {
        blogService.deleteComment(commentId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //發送檢舉
    @PostMapping("/blogs/{blogId}/reports")
    public ResponseEntity<BlogReport> reportBlog(@PathVariable Integer blogId, @RequestBody @Valid BlogReportRequest blogReportRequest) {

        Integer blogReportId = blogService.reportBlog(blogId , blogReportRequest);

         BlogReport blogReport = blogService.getBlogReportById(blogReportId);

        return ResponseEntity.status(HttpStatus.CREATED).body(blogReport);
    }



    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<BlogCommentReport> reportComment(@PathVariable Integer commentId, @RequestBody @Valid BlogReportRequest request) {
        Integer reportCommentId = blogService.reportComment(commentId ,request);

        BlogCommentReport reportComment = blogService.getCommentReportById(reportCommentId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(reportComment);
    }


}
