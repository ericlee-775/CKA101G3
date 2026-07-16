package com.farmily.blog.rowmapper;

import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.model.BlogComment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

// 只給「留言列表」用：跟 BlogCommentRowMapper 一樣，但多帶留言者顯示名稱
// （需搭配 getBlogComments 的 SQL 額外 SELECT 出 author_name 一欄）
public class BlogCommentCardRowMapper implements RowMapper<BlogComment> {

    @Override
    public BlogComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(rs.getInt("comment_id"));
        blogComment.setBlogId(rs.getInt("blog_id"));
        blogComment.setUserId(rs.getInt("user_id"));
        blogComment.setCommentTime(rs.getTimestamp("comment_time"));
        blogComment.setCommentPost(rs.getString("comment_post"));
        blogComment.setCommentLike(rs.getInt("comment_like"));
        blogComment.setCommentStatus(BlogStatus.valueOf(rs.getString("comment_status")));

        // 留言者顯示名稱：會員暱稱優先，退回姓名（由 SQL 的 COALESCE 決定）
        blogComment.setAuthorName(rs.getString("author_name"));

        return blogComment;
    }
}
