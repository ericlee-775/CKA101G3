package com.farmily.news.controller;

import com.farmily.blog.util.Page;
import com.farmily.news.dto.NewsRequest;
import com.farmily.news.dto.NewsResponse;
import com.farmily.news.service.NewsService;
import com.farmily.user.security.AdminUserDetails;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Validated
@RestController
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;


    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/news")
    public Page<NewsResponse> getNewsAll(
            @RequestParam(defaultValue = "0") @Min(1) Integer offset,
            @RequestParam(defaultValue = "5") @Max(1000) @Min(1) Integer limit) {

        return newsService.getPublishedNews(offset, limit);   // 只回已發布(ACTIVE)
    }

    @GetMapping("/news/{newsId}")
    public NewsResponse getNewsOne(@PathVariable Integer newsId) {

        return newsService.getPublishedNewsOne(newsId);
    }

    @GetMapping("/news/{newsId}/image")
    public void getNewsImg(HttpServletResponse res, @PathVariable Integer newsId) throws IOException {

        byte[] img = newsService.getNewsImage(newsId);
        ServletOutputStream out = res.getOutputStream();

        if (img != null && img.length > 0) {
            // 自動判斷 jpg/png/gif，判不出就當 jpeg
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
            res.setContentType(type != null ? type : MediaType.IMAGE_JPEG_VALUE);
            out.write(img);
        } else {
            res.setStatus(HttpStatus.NOT_FOUND.value());   // 沒圖回 404，前端 @error 會自動隱藏
        }
    }

    //管理員
    @GetMapping("/admin/news")
    public Page<NewsResponse> getAllNews(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "5") Integer limit) {

        return newsService.getAllNews(offset, limit);
    }

    @GetMapping("/admin/news/{newsId}")
    public NewsResponse getAdminNewsOne(@PathVariable Integer newsId) {

        return newsService.getNewsOne(newsId);
    }


    @PostMapping(value = "/admin/news" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsResponse> createNews(
            @AuthenticationPrincipal AdminUserDetails me,
            @ModelAttribute @Valid NewsRequest request) {

        NewsResponse response = newsService.createNews(me.getAdminId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/admin/news/{newsId}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsResponse updateNews(@PathVariable Integer newsId, @ModelAttribute @Valid NewsRequest request) {
        return newsService.updateNews(newsId, request);
    }



    @PatchMapping("/admin/news/{newsId}/status")
    public NewsResponse changeNewsStatus(@PathVariable Integer newsId, @RequestParam Boolean status) {
        return newsService.changeNewsStatus(newsId, status);
    }

    @DeleteMapping("/admin/news/{newsId}")
    public ResponseEntity<Void> deleteNews(@PathVariable Integer newsId) {

        newsService.deleteNews(newsId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
