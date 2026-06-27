package com.farmily.blog.repository;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.model.BlogCommentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentReportRepository extends JpaRepository<BlogCommentReport, Integer> {

    Page<BlogCommentReport> findByReportStatus(BlogReportStatus reportStatus, Pageable pageable);

    Page<BlogCommentReport> findByCommentId(Integer commentId, Pageable pageable);

    long countByReportStatus(BlogReportStatus reportStatus);
}
