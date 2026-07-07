package com.farmily.blog.controller;

import com.farmily.blog.dto.BlogAuthor;
import com.farmily.blog.dto.BlogPhotoResponse;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogResponse;
import com.farmily.blog.service.BlogService;
import com.farmily.blog.util.Page;
import com.farmily.user.security.MemberUserDetails;
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
@RequestMapping("/api/member/blogs")
public class UserBlogController {
    private final BlogService blogService;

    public UserBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public Page<BlogResponse> getMyBlogs(
            @AuthenticationPrincipal MemberUserDetails me,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit) {
        return blogService.getMyBlogs(BlogAuthor.ofMember(me.getUserId()), offset, limit);
    }

    @GetMapping("/{blogId}")
    public BlogResponse getMyBlog(
            @AuthenticationPrincipal MemberUserDetails me,
            @PathVariable Integer blogId) {
        return blogService.getMyBlog(BlogAuthor.ofMember(me.getUserId()), blogId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponse> createMyBlog(
            @AuthenticationPrincipal MemberUserDetails me,
            @ModelAttribute @Valid BlogRequest blogRequest,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        if (file != null && !file.isEmpty()) {
            blogRequest.setBlogImg(file.getBytes());
        }
        BlogResponse response = blogService.createMyBlog(BlogAuthor.ofMember(me.getUserId()), blogRequest, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{blogId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BlogPhotoResponse>> addPhotos(
            @AuthenticationPrincipal MemberUserDetails me,
            @PathVariable Integer blogId,
            @RequestParam("photos") List<MultipartFile> photos) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.addMyBlogPhotos(BlogAuthor.ofMember(me.getUserId()), blogId, photos));
    }

    @PutMapping(value = "/{blogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BlogResponse updateMyBlog(
            @AuthenticationPrincipal MemberUserDetails me,
            @PathVariable Integer blogId,
            @ModelAttribute @Valid BlogRequest blogRequest,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            blogRequest.setBlogImg(file.getBytes());
        }
        return blogService.updateMyBlog(BlogAuthor.ofMember(me.getUserId()), blogId, blogRequest);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteMyBlog(
            @AuthenticationPrincipal MemberUserDetails me,
            @PathVariable Integer blogId) {
        blogService.deleteMyBlog(BlogAuthor.ofMember(me.getUserId()), blogId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal MemberUserDetails me,
            @PathVariable Integer photoId) {
        blogService.deleteMyPhoto(BlogAuthor.ofMember(me.getUserId()), photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
