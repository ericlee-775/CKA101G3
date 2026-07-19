package com.farmily.blog.controller;

import com.farmily.blog.dto.*;
import com.farmily.blog.model.Blog;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.farmily.user.security.MemberUserDetails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class BlogController {

    private final BlogService blogService;

    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /* ===== 公開 ===== */

    @GetMapping("/blogs/types")
    public List<BlogTypeResponse> types() {return blogService.listTypes();}

    @GetMapping("/blogs")
    public Page<BlogResponse> getBlogAll(
            //查詢條件
            @RequestParam(required = false) Integer blogTypeId,
            @RequestParam(required = false) String search,
            //排序
            @RequestParam(defaultValue = "blog_time") String orderBy,
            @RequestParam(defaultValue = "desc") String sort,
            //分頁
            @RequestParam(defaultValue = "8") @Max(1000) @Min(0) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        BlogQueryParms parms = new BlogQueryParms();   // 把 request 參數組成查詢物件（輸入對應，留 controller OK）
        parms.setBlogTypeId(blogTypeId);
        parms.setSearch(search);
        parms.setOrderBy(orderBy);
        parms.setSort(sort);
        parms.setLimit(limit);
        parms.setOffset(offset);
        parms.setStatus("VISIBLE");

        return blogService.getBlogPage(parms);         // 查詢/算總數/組Page/轉DTO 全在 service
    }

    @GetMapping("/blogs/{blogId}")
    public BlogResponse getBlogOne(@PathVariable Integer blogId) {
        //從service 去抓blogId
       return blogService.getBlogDetail(blogId);
    }

    @GetMapping("/blogs/{blogId}/comments")
    public List<BlogCommentResponse> getBlogComments( @PathVariable Integer blogId)  {

        return blogService.getBlogComments(blogId);
    }
    //取得部落格照片集 photoId
    @GetMapping("/blogs/{blogId}/photos")
    public ResponseEntity<List<BlogPhotoResponse>> getBlogPhotos(@PathVariable Integer blogId) {

        List<BlogPhotoResponse> blogPhotos = blogService.getBlogPhotos(blogId);

        return ResponseEntity.ok(blogPhotos);
    }
    //用photoId去找圖檔顯示出來
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
    


    /* ===== 互動 ===== */
    @PostMapping("/blogs/{blogId}/like")
    public BlogResponse likeBlog(@PathVariable Integer blogId,
                                 @AuthenticationPrincipal MemberUserDetails me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請以會員身分登入");
        return blogService.likeBlog(blogId, me.getUserId());   // 從登入會員取，不信任前端
    }

    // 進頁面時查目前會員按過沒（公開端點，未登入 me 為 null → liked=false）
    @GetMapping("/blogs/{blogId}/like-status")
    public BlogResponse likeStatus(@PathVariable Integer blogId,
                                   @AuthenticationPrincipal MemberUserDetails me) {
        Integer userId = (me != null) ? me.getUserId() : null;
        return blogService.getLikeStatus(blogId, userId);
    }


    @PostMapping("/blogs/{blogId}/comments")
    public ResponseEntity<BlogCommentResponse> addBlogComment(@PathVariable Integer blogId,
                                                              @RequestBody BlogCommentRequest request,
                                                              @AuthenticationPrincipal MemberUserDetails me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請以會員身分登入");
        request.setBlogId(blogId);
        request.setUserId(me.getUserId());   // 留言者從登入會員取
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.addBlogComment(request));
    }

    @DeleteMapping("/blogs/comments/{commentId}")
    public ResponseEntity<?> deleteComment (@PathVariable Integer commentId,
                                            @AuthenticationPrincipal MemberUserDetails me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請以會員身分登入");
        blogService.deleteComment(commentId, me.getUserId());   // 只能刪自己的留言，服務層驗證

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //發送檢舉
    @PostMapping("/blogs/{blogId}/reports")
    public ResponseEntity<Void> reportBlog(@PathVariable Integer blogId,
                                           @RequestBody @Valid BlogReportRequest blogReportRequest,
                                           @AuthenticationPrincipal MemberUserDetails me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請以會員身分登入");
        blogReportRequest.setUserId(me.getUserId());   // 檢舉人從登入會員取
        blogService.reportBlog(blogId, blogReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build(); //201
    }



    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<Void> reportComment(@PathVariable Integer commentId,
                                              @RequestBody @Valid BlogReportRequest request,
                                              @AuthenticationPrincipal MemberUserDetails me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請以會員身分登入");
        request.setUserId(me.getUserId());   // 檢舉人從登入會員取
        blogService.reportComment(commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build(); //201
    }


}
