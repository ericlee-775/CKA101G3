package com.farmily.blog.controller;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.model.BlogCommentReport;
import com.farmily.blog.model.BlogReport;
import com.farmily.blog.service.AdminBlogReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminBlogController {


    private final AdminBlogReportService adminBlogReportService;
    //建構子注入

    public AdminBlogController(AdminBlogReportService adminBlogReportService) {
        this.adminBlogReportService = adminBlogReportService;
    }

    //文章檢舉列表(分頁)
    @GetMapping("/blog-reports")
    public Page<BlogReport> listBlogReports(
            @RequestParam(required = false) BlogReportStatus status,
            @PageableDefault(size = 10, sort = "reportTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminBlogReportService.getBlogReports(status, pageable);
    }

    // 通過文章檢舉（維持顯示）
    @PatchMapping("/blog-reports/{reportId}/approve")
    public BlogReport approveBlogReport(@PathVariable Integer reportId, @RequestParam Integer adminId) {
        return adminBlogReportService.reviewBlogReport(reportId, adminId, true);
    }

    // 駁回文章檢舉（隱藏文章）
    @PatchMapping("/blog-reports/{reportId}/reject")
    public BlogReport rejectBlogReport(@PathVariable Integer reportId, @RequestParam Integer adminId) {
        return adminBlogReportService.reviewBlogReport(reportId, adminId, false);
    }

    // 留言檢舉列表（分頁）
    @GetMapping("/comment-reports")
    public Page<BlogCommentReport> listCommentReports(
            @RequestParam(required = false) BlogReportStatus status,
            @PageableDefault(size = 10, sort = "reportTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminBlogReportService.getCommentReports(status, pageable);
    }

    // 通過留言檢舉
    @PatchMapping("/comment-reports/{reportId}/approve")
    public BlogCommentReport approveCommentReport(@PathVariable Integer reportId, @RequestParam Integer adminId) {
        return adminBlogReportService.reviewCommentReport(reportId, adminId, true);
    }

    // 駁回留言檢舉
    @PatchMapping("/comment-reports/{reportId}/reject")
    public BlogCommentReport rejectCommentReport(@PathVariable Integer reportId, @RequestParam Integer adminId) {
        return adminBlogReportService.reviewCommentReport(reportId, adminId, false);
    }

    //文章檢舉數量統計
    @GetMapping("/blog-reports/summary")
    public Map<String, Long> getBlogReportSummary() {
        return adminBlogReportService.getBlogReportSummary();
    }


}
