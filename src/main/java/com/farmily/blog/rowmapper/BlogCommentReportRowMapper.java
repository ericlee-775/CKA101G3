package com.farmily.blog.rowmapper;

import com.farmily.blog.constant.BlogReportStatus;
import com.farmily.blog.model.BlogCommentReport;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BlogCommentReportRowMapper implements RowMapper<BlogCommentReport> {

    @Override
    public BlogCommentReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        BlogCommentReport blogCommentReport = new BlogCommentReport();
        blogCommentReport.setReportCommentId(rs.getInt("report_comment_id"));
        blogCommentReport.setCommentId(rs.getInt("comment_id"));
        blogCommentReport.setBlogId(rs.getInt("blog_id"));
        blogCommentReport.setUserId(rs.getInt("user_id"));
        blogCommentReport.setAdminId((Integer) rs.getObject("admin_id"));
        blogCommentReport.setReportTime(rs.getTimestamp("report_time"));
        blogCommentReport.setReportReason(rs.getString("report_reason"));
        blogCommentReport.setReportStatus(BlogReportStatus.valueOf(rs.getString("report_status")));

        return blogCommentReport;
    }
}
