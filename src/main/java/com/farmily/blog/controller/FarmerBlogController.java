package com.farmily.blog.controller;

import com.farmily.blog.dto.BlogAuthor;
import com.farmily.blog.dto.BlogPhotoResponse;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogResponse;
import com.farmily.blog.service.BlogService;
import com.farmily.blog.util.Page;
import com.farmily.user.security.FarmerUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/farmer/blogs")
public class FarmerBlogController {

    private final BlogService blogService;

    public FarmerBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    // 我的產地日記列表
    @GetMapping
    public Page<BlogResponse> getMyBlogs(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit) {
        return blogService.getMyBlogs(BlogAuthor.ofFarmer(me.getFarmerId()), offset, limit);
    }

    // 查看單篇
    @GetMapping("/{blogId}")
    public BlogResponse getMyBlog(
            @AuthenticationPrincipal FarmerUserDetails me,
            @PathVariable Integer blogId) {
        return blogService.getMyBlog(BlogAuthor.ofFarmer(me.getFarmerId()), blogId);
    }

    // 新增
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponse> createMyBlog(
            @AuthenticationPrincipal FarmerUserDetails me,
            @ModelAttribute @Valid BlogRequest blogRequest,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        if (file != null && !file.isEmpty()) {
            blogRequest.setBlogImg(file.getBytes());
        }
        BlogResponse response = blogService.createMyBlog(BlogAuthor.ofFarmer(me.getFarmerId()), blogRequest, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //新增照片集
    @PostMapping(value = "/{blogId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BlogPhotoResponse>> addPhotos(
            @AuthenticationPrincipal FarmerUserDetails me,
            @PathVariable Integer blogId,
            @RequestParam("photos") List<MultipartFile> photos) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.addMyBlogPhotos(BlogAuthor.ofFarmer(me.getFarmerId()), blogId, photos));
    }

    // 編輯
    @PutMapping(value = "/{blogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BlogResponse updateMyBlog(
            @AuthenticationPrincipal FarmerUserDetails me,
            @PathVariable Integer blogId,
            @ModelAttribute @Valid BlogRequest blogRequest,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            blogRequest.setBlogImg(file.getBytes());
        }
        return blogService.updateMyBlog(BlogAuthor.ofFarmer(me.getFarmerId()), blogId, blogRequest);
    }

    // 刪除
    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteMyBlog(
            @AuthenticationPrincipal FarmerUserDetails me,
            @PathVariable Integer blogId) {
        blogService.deleteMyBlog(BlogAuthor.ofFarmer(me.getFarmerId()), blogId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //刪除照片集
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal FarmerUserDetails me,
            @PathVariable Integer photoId) {
        blogService.deleteMyPhoto(BlogAuthor.ofFarmer(me.getFarmerId()), photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
