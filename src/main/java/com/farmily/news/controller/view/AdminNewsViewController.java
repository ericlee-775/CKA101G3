package com.farmily.news.controller.view;

import com.farmily.blog.util.Page;
import com.farmily.news.dto.NewsRequest;
import com.farmily.news.dto.NewsResponse;
import com.farmily.news.service.NewsService;
import com.farmily.user.security.AdminUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Timestamp;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsViewController {

    private final NewsService newsService;

    public AdminNewsViewController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public String listAll(ModelMap model) {
        Page<NewsResponse> page = newsService.getAllNews(0, 1000);
        model.addAttribute("newsListData", page.getResults());
        return "back-end/admin/listAllNews";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal AdminUserDetails me,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String publishTime,
                         @RequestParam(required = false) MultipartFile coverImg,
                         RedirectAttributes redirectAttributes) throws IOException {

        NewsRequest request = new NewsRequest();
        request.setTitle(title);
        request.setContent(content);

        try {
            request.setPublishTime(parsePublishTime(publishTime));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "（發布時間格式錯誤）");
            return "redirect:/admin/news";
        }

        if (coverImg != null && !coverImg.isEmpty()) {
            request.setCoverImg(coverImg.getBytes());
        }

        newsService.createNews(me.getAdminId(), request);
        redirectAttributes.addFlashAttribute("success", "（已新增最新消息）");
        return "redirect:/admin/news";
    }

    private Timestamp parsePublishTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String text = value.trim().replace("T", " ");
        if (text.length() == 16) {
            text += ":00";
        }

        return Timestamp.valueOf(text);
    }

    @PostMapping("/{newsId}/status")
    public String changeStatus(@PathVariable Integer newsId,
                               @RequestParam Boolean publish,
                               RedirectAttributes redirectAttributes) {
        newsService.changeNewsStatus(newsId, publish);
        redirectAttributes.addFlashAttribute("success", publish ? "（已上架）" : "（已下架）");
        return "redirect:/admin/news";
    }

    @PostMapping("/{newsId}/update")
    public String update(@PathVariable Integer newsId,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String publishTime,
                         @RequestParam(required = false) MultipartFile coverImg,
                         RedirectAttributes redirectAttributes) throws IOException {

        NewsRequest request = new NewsRequest();
        request.setTitle(title);
        request.setContent(content);

        try {
            request.setPublishTime(parsePublishTime(publishTime));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "（發布時間格式錯誤）");
            return "redirect:/admin/news";
        }

        if (coverImg != null && !coverImg.isEmpty()) {
            request.setCoverImg(coverImg.getBytes());
        }

        newsService.updateNews(newsId, request);
        redirectAttributes.addFlashAttribute("success", "（已修改最新消息）");
        return "redirect:/admin/news";
    }

    @PostMapping("/{newsId}/delete")
    public String delete(@PathVariable Integer newsId,
                         RedirectAttributes redirectAttributes) {
        newsService.deleteNews(newsId);
        redirectAttributes.addFlashAttribute("success", "（已刪除最新消息）");
        return "redirect:/admin/news";
    }
}