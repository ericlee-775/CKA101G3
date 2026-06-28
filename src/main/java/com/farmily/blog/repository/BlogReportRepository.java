package com.farmily.blog.repository;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.model.BlogReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogReportRepository extends JpaRepository<BlogReport, Integer> {

    // 方法名 = 查詢條件：WHERE report_status = ?，自動套用分頁/排序
    Page<BlogReport> findByReportStatus(BlogReportStatus reportStatus, Pageable pageable);

    // 同一篇文章的所有檢舉（管理員想看某篇被檢舉幾次時用）
    Page<BlogReport> findByBlogId(Integer blogId, Pageable pageable);

    // 衍生查詢也能做統計：某狀態的檢舉數量
    long countByReportStatus(BlogReportStatus reportStatus);
}
