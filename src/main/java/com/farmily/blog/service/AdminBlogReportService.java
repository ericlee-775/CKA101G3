package com.farmily.blog.service;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.model.BlogCommentReport;
import com.farmily.blog.model.BlogReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;


public interface AdminBlogReportService {

    //文章檢舉列表(分頁)
    Page<BlogReport> getBlogReports(BlogReportStatus status, Pageable pageable);

    // approve = true 通過(維持顯示) false 駁回(隱藏)
    BlogReport reviewBlogReport(Integer blogReportId, Integer adminId , boolean approve);

    // 留言檢舉列表（分頁）
    Page<BlogCommentReport> getCommentReports(BlogReportStatus status, Pageable pageable);


    BlogCommentReport reviewCommentReport(Integer reportCommentId, Integer adminId, boolean approve);

    //文章檢舉的數量統計
    Map<String, Long> getBlogReportSummary();



}
