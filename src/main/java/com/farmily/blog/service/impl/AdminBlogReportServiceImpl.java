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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

        // 通知收件人＝文章作者。小農作者(farmerId)目前 sender 無 farmer 版，暫只通知會員作者
        // TODO: 待 notification 負責人在 NotificationSender 加 farmer 版後，補上小農作者通知
        Integer authorUserId = blog.getUserId();

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            blog.setBlogStatus(BlogStatus.VISIBLE);
            // 維持顯示：通知作者「檢舉未成立、文章維持顯示」
            if (authorUserId != null) {
                Integer blogId = blog.getBlogId();
                afterCommit(() -> notificationSender.sendBlogReportVisible(authorUserId, blogId));
            }
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            blog.setBlogStatus(BlogStatus.HIDDEN);
            // 隱藏：通知作者「文章因檢舉被隱藏」，附上檢舉原因
            if (authorUserId != null) {
                Integer blogId = blog.getBlogId();
                String reason = report.getReportReason();
                afterCommit(() -> notificationSender.sendBlogReportHidden(authorUserId, blogId, reason));
            }
        }
        report.setAdminId(adminId);
        return report; // 改完欄位即可，交易結束 JPA 自動 UPDATE（不用 save）
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

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            comment.setCommentStatus(BlogStatus.VISIBLE);
            // 維持顯示：通知留言作者「檢舉未成立、留言維持顯示」
            if (commentAuthorUserId != null) {
                afterCommit(() -> notificationSender.sendBlogCommentReportVisible(commentAuthorUserId, blogId));
            }
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            comment.setCommentStatus(BlogStatus.HIDDEN);
            // 隱藏：通知留言作者「留言因檢舉被隱藏」，附上檢舉原因
            if (commentAuthorUserId != null) {
                String reason = report.getReportReason();
                afterCommit(() -> notificationSender.sendBlogCommentReportHidden(commentAuthorUserId, blogId, reason));
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

    /*
     * 把通知動作排到「交易成功 commit 之後」才執行。
     * 原因：本方法標了 @Transactional，而 NotificationSender 最終呼叫的 sendOneNotif 也是 @Transactional，
     * 預設 propagation=REQUIRED 會「加入」目前這個交易。若通知過程丟例外，整個交易會被標記 rollback-only，
     * 就算這裡 try/catch 也救不回來 —— 審核結果(文章顯示/隱藏)會跟著被回滾。
     * 用 afterCommit：等審核交易確定 commit 後，才在「新的一個交易」裡發通知；通知失敗也只影響通知本身，
     * 不會回滾已完成的審核。
     */
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (Exception e) {
                        // 通知失敗不影響審核結果，吞掉即可（可視需要改成記 log）
                    }
                }
            });
        } else {
            // 理論上不會走到（本方法一定在交易內）；保險起見仍執行一次
            try {
                action.run();
            } catch (Exception e) {
                // 同上
            }
        }
    }

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
            v.setBlogContent(blog.getBlogContent());
        }
        return v;
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

