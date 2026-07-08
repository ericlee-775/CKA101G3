package com.farmily.blog.controller;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.dto.BlogAdminReport;
import com.farmily.blog.service.AdminBlogReportService;
import com.farmily.user.security.AdminUserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 後台「部落格檢舉審核」頁面（Thymeleaf + 表單 POST，非 REST）
@Controller
@RequestMapping("/admin")
public class AdminBlogReportViewController {

    private final AdminBlogReportService adminBlogReportService;

    public AdminBlogReportViewController(AdminBlogReportService adminBlogReportService) {
        this.adminBlogReportService = adminBlogReportService;
    }

    // 檢舉清單頁：文章檢舉 + 留言檢舉
    @GetMapping("/blog-reports")
    public String list(Model model) {
        Pageable pageable = PageRequest.of(0, 200, Sort.by("reportTime").descending());
        List<BlogAdminReport> blog = adminBlogReportService.getBlogReports(null, pageable).getContent();
        List<BlogAdminReport> comment = adminBlogReportService.getCommentReports(null, pageable).getContent();

        // 分層：待審(PENDING) 放上面可操作、已處理放下面當歷史
        model.addAttribute("pendingBlogReports",
                blog.stream().filter(r -> r.getReportStatus() == BlogReportStatus.PENDING).toList());
        model.addAttribute("historyBlogReports",
                blog.stream().filter(r -> r.getReportStatus() != BlogReportStatus.PENDING).toList());
        model.addAttribute("pendingCommentReports",
                comment.stream().filter(r -> r.getReportStatus() == BlogReportStatus.PENDING).toList());
        model.addAttribute("historyCommentReports",
                comment.stream().filter(r -> r.getReportStatus() != BlogReportStatus.PENDING).toList());
        model.addAttribute("summary", adminBlogReportService.getBlogReportSummary());
        return "back-end/admin/listBlogReports";
    }

    // 文章檢舉：通過（維持顯示）/ 駁回（隱藏文章）
    @PostMapping("/blog-reports/{reportId}/approve")
    public String approveBlog(@PathVariable Integer reportId, @AuthenticationPrincipal AdminUserDetails me) {
        adminBlogReportService.reviewBlogReport(reportId, me.getAdminId(), true);
        return "redirect:/admin/blog-reports";
    }

    @PostMapping("/blog-reports/{reportId}/reject")
    public String rejectBlog(@PathVariable Integer reportId, @AuthenticationPrincipal AdminUserDetails me) {
        adminBlogReportService.reviewBlogReport(reportId, me.getAdminId(), false);
        return "redirect:/admin/blog-reports";
    }

    // 留言檢舉：通過 / 駁回
    @PostMapping("/comment-reports/{reportId}/approve")
    public String approveComment(@PathVariable Integer reportId, @AuthenticationPrincipal AdminUserDetails me) {
        adminBlogReportService.reviewCommentReport(reportId, me.getAdminId(), true);
        return "redirect:/admin/blog-reports";
    }

    @PostMapping("/comment-reports/{reportId}/reject")
    public String rejectComment(@PathVariable Integer reportId, @AuthenticationPrincipal AdminUserDetails me) {
        adminBlogReportService.reviewCommentReport(reportId, me.getAdminId(), false);
        return "redirect:/admin/blog-reports";
    }
}
