package com.farmily.blog.service.impl;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.model.Blog;
import com.farmily.blog.model.BlogComment;
import com.farmily.blog.model.BlogCommentReport;
import com.farmily.blog.model.BlogReport;
import com.farmily.blog.repository.BlogCommentReportRepository;
import com.farmily.blog.repository.BlogCommentRepository;
import com.farmily.blog.repository.BlogReportRepository;
import com.farmily.blog.repository.BlogRepository;
import com.farmily.blog.service.AdminBlogReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminBlogReportServiceImpl implements AdminBlogReportService {

    private final BlogReportRepository blogReportRepository;
    private final BlogRepository blogRepository;
    private final BlogCommentReportRepository blogCommentReportRepository;
    private final BlogCommentRepository blogCommentRepository;

    @Autowired
    public AdminBlogReportServiceImpl(BlogReportRepository blogReportRepository,
                                      BlogRepository blogRepository,
                                      BlogCommentReportRepository blogCommentReportRepository,
                                      BlogCommentRepository blogCommentRepository) {
        this.blogReportRepository = blogReportRepository;
        this.blogRepository = blogRepository;
        this.blogCommentReportRepository = blogCommentReportRepository;
        this.blogCommentRepository = blogCommentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogReport> getBlogReports(BlogReportStatus status, Pageable pageable) {
        if(status == null) {
            return blogReportRepository.findAll(pageable); //
        }
        return blogReportRepository.findByReportStatus(status, pageable);
    }


    @Override
    @Transactional
    public BlogReport reviewBlogReport(Integer reportId, Integer adminId, boolean approve) {
        BlogReport report = blogReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "檢舉不存在: " + reportId));

        Blog blog = blogRepository.findById(report.getBlogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在: " + report.getBlogId()));

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            blog.setBlogStatus(BlogStatus.VISIBLE);
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            blog.setBlogStatus(BlogStatus.HIDDEN);
        }
        report.setAdminId(adminId);
        return report; // 改完欄位即可，交易結束 JPA 自動 UPDATE（不用 save）
    }

    @Override
    public Page<BlogCommentReport> getCommentReports(BlogReportStatus status, Pageable pageable) {
        if (status == null) {
            return blogCommentReportRepository.findAll(pageable);
        }
        return blogCommentReportRepository.findByReportStatus(status, pageable);
    }


    @Override
    @Transactional
    public BlogCommentReport reviewCommentReport(Integer reportId, Integer adminId, boolean approve) {
        BlogCommentReport report = blogCommentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "留言檢舉不存在: " + reportId));

        BlogComment comment = blogCommentRepository.findById(report.getCommentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "留言不存在: " + report.getCommentId()));

        if (approve) {
            report.setReportStatus(BlogReportStatus.APPROVED_VISIBLE);
            comment.setCommentStatus(BlogStatus.VISIBLE);
        } else {
            report.setReportStatus(BlogReportStatus.REJECTED_HIDDEN);
            comment.setCommentStatus(BlogStatus.HIDDEN);
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
}
