package com.farmily.blog.service.impl;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.dto.BlogAdminReport;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;
import com.farmily.blog.model.BlogCommentReport;
import com.farmily.blog.model.BlogReport;
import com.farmily.blog.repository.BlogCommentReportRepository;
import com.farmily.blog.repository.BlogCommentRepository;
import com.farmily.blog.repository.BlogReportRepository;
import com.farmily.blog.repository.BlogRepository;
import com.farmily.blog.service.AdminBlogReportService;
import com.farmily.notification.service.NotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminBlogReportServiceImpl implements AdminBlogReportService {

    private final BlogReportRepository blogReportRepository;
    private final BlogRepository blogRepository;
    private final BlogCommentReportRepository blogCommentReportRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final NotificationSender notificationSender;

    @Autowired
    public AdminBlogReportServiceImpl(BlogReportRepository blogReportRepository,
                                      BlogRepository blogRepository,
                                      BlogCommentReportRepository blogCommentReportRepository,
                                      BlogCommentRepository blogCommentRepository,
                                      NotificationSender notificationSender) {
        this.blogReportRepository = blogReportRepository;
        this.blogRepository = blogRepository;
        this.blogCommentReportRepository = blogCommentReportRepository;
        this.blogCommentRepository = blogCommentRepository;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogAdminReport> getBlogReports(BlogReportStatus status, Pageable pageable) {
        Page<BlogReport> page;
        if(status == null) {
            page =  blogReportRepository.findAll(pageable); //
        } else {
            page = blogReportRepository.findByReportStatus(status, pageable);
            }

        // 批次撈這頁所有被檢舉文章（避免每筆 findById 造成 N+1），做成 id -> Blog 的對照表
        Map<Integer, Blog> blogMap = loadBlogs(
                page.getContent().stream().map(BlogReport::getBlogId).toList());

        List<BlogAdminReport> content = page.getContent().stream()
                .map(r -> toView(r, blogMap.get(r.getBlogId())))
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }


    @Override
    @Transactional
    public BlogReport reviewBlogReport(Integer reportId, Integer adminId, boolean approve) {
        BlogReport report = blogReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "檢舉不存在: " + reportId));

        Blog blog = blogRepository.findById(report.getBlogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在: " + report.getBlogId()));

        // 通知收件人＝文章作者：會員作者走會員版、小農作者走小農版
        Integer authorUserId = blog.getUserId();
        Integer authorFarmerId = blog.getFarmerId();
        Integer blogId = blog.getBlogId();
        String title = blog.getBlogTitle() != null ? blog.getBlogTitle() : "";  // sender 的 Map.of 不吃 null

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            blog.setBlogStatus(BlogStatus.VISIBLE);
            // 維持顯示：通知作者「檢舉未成立、文章維持顯示」
            if (authorUserId != null) {
                notificationSender.sendBlogReportVisible(authorUserId, blogId, title);
            } else if (authorFarmerId != null) {
                notificationSender.sendFarmerBlogReportVisible(authorFarmerId, blogId, title);
            }
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            blog.setBlogStatus(BlogStatus.HIDDEN);
            // 隱藏：通知作者「文章因檢舉被隱藏」，附上檢舉原因
            String reason = report.getReportReason() != null ? report.getReportReason() : "";
            if (authorUserId != null) {
                notificationSender.sendBlogReportHidden(authorUserId, blogId, reason, title);
            } else if (authorFarmerId != null) {
                notificationSender.sendFarmerBlogReportHidden(authorFarmerId, blogId, reason, title);
            }
        }
        report.setAdminId(adminId);
        // 通知與審核在同一交易一起 commit（可靠送達）；改完欄位交易結束 JPA 自動 UPDATE
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogAdminReport> getCommentReports(BlogReportStatus status, Pageable pageable) {
        Page<BlogCommentReport> page;
        if (status == null) {
            page = blogCommentReportRepository.findAll(pageable);
        } else {
            page = blogCommentReportRepository.findByReportStatus(status, pageable);
        }

        // 留言檢舉要顯示：留言內容 + 所屬文章標題，所以兩張表都批次撈
        Map<Integer, BlogComment> commentMap = loadComments(
                page.getContent().stream().map(BlogCommentReport::getCommentId).toList());
        Map<Integer, Blog> blogMap = loadBlogs(
                page.getContent().stream().map(BlogCommentReport::getBlogId).toList());

        List<BlogAdminReport> content = page.getContent().stream()
                .map(r -> toView(r, commentMap.get(r.getCommentId()), blogMap.get(r.getBlogId())))
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
        }


    @Override
    @Transactional
    public BlogCommentReport reviewCommentReport(Integer reportId, Integer adminId, boolean approve) {
        BlogCommentReport report = blogCommentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "留言檢舉不存在: " + reportId));

        BlogComment comment = blogCommentRepository.findById(report.getCommentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "留言不存在: " + report.getCommentId()));

        // 通知收件人＝留言作者（留言一定是會員），不是檢舉人(report.getUserId())
        Integer commentAuthorUserId = comment.getUserId();
        Integer blogId = report.getBlogId();
        // 標題取「留言所在文章」的標題（文章可能已刪則給空字串，sender 的 Map.of 不吃 null）
        String title = blogRepository.findById(blogId).map(Blog::getBlogTitle).orElse("");

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            comment.setCommentStatus(BlogStatus.VISIBLE);
            // 維持顯示：通知留言作者「檢舉未成立、留言維持顯示」
            if (commentAuthorUserId != null) {
                notificationSender.sendBlogCommentReportVisible(commentAuthorUserId, blogId, title);
            }
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            comment.setCommentStatus(BlogStatus.HIDDEN);
            // 隱藏：通知留言作者「留言因檢舉被隱藏」，附上檢舉原因
            if (commentAuthorUserId != null) {
                String reason = report.getReportReason() != null ? report.getReportReason() : "";
                notificationSender.sendBlogCommentReportHidden(commentAuthorUserId, blogId, reason, title);
            }
        }
        report.setAdminId(adminId);
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getBlogReportSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("pending" , blogReportRepository.countByReportStatus(BlogReportStatus.PENDING));
        summary.put("approvedVisible" , blogReportRepository.countByReportStatus(BlogReportStatus.APPROVED_VISIBLE));
        summary.put("rejectedHidden" , blogReportRepository.countByReportStatus(BlogReportStatus.REJECTED_HIDDEN));
        return summary;
    }

    /* ===== 方法 ===== */

    // 依 id 清單批次撈文章，回傳 id -> Blog 對照表（找不到的 id 就不在 map 裡）
    private Map<Integer, Blog> loadBlogs(List<Integer> blogIds) {
        return blogRepository.findAllById(blogIds).stream()
                .collect(Collectors.toMap(Blog::getBlogId, Function.identity()));
    }

    // 依 id 清單批次撈留言，回傳 id -> BlogComment 對照表
    private Map<Integer, BlogComment> loadComments(List<Integer> commentIds) {
        return blogCommentRepository.findAllById(commentIds).stream()
                .collect(Collectors.toMap(BlogComment::getCommentId, Function.identity()));
    }

    // BlogReport → DTO（blog 可能為 null：文章已被刪，仍要顯示檢舉紀錄）
    private BlogAdminReport toView(BlogReport r, Blog blog) {
        BlogAdminReport v = new BlogAdminReport();
        v.setReportId(r.getBlogReportId());      // 文章檢舉主鍵
        v.setTargetType("BLOG");
        v.setBlogId(r.getBlogId());
        v.setReportReason(r.getReportReason());
        v.setReportStatus(r.getReportStatus());
        v.setReportTime(r.getReportTime());
        if (blog != null) {
            v.setBlogTitle(blog.getBlogTitle());
            v.setBlogContent(stripHtml(blog.getBlogContent()));   // 內容可能是編輯器產的 HTML，後台只看純文字
        }
        return v;
    }

    // 去掉 HTML 標籤：新文章內容是 RichTextEditor 產的 HTML(<p>…</p>)，後台審核顯示純文字即可、也避免 XSS
    private static String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]+>", " ")   // 每個標籤換成一個空白（避免 </p><p> 把字黏在一起）
                   .replaceAll("\\s+", " ")       // 多個空白收成一個
                   .trim();
    }

    // BlogCommentReport → DTO（comment / blog 都可能為 null）
    private BlogAdminReport toView(BlogCommentReport r, BlogComment comment, Blog blog) {
        BlogAdminReport v = new BlogAdminReport();
        v.setReportId(r.getReportCommentId());   // 留言檢舉主鍵（名字不同）
        v.setTargetType("COMMENT");
        v.setBlogId(r.getBlogId());
        v.setCommentId(r.getCommentId());
        v.setReportReason(r.getReportReason());
        v.setReportStatus(r.getReportStatus());
        v.setReportTime(r.getReportTime());
        if (comment != null) {
            v.setCommentContent(comment.getCommentPost());
        }
        if (blog != null) {
            v.setBlogTitle(blog.getBlogTitle());
        }
        return v;
    }


}

