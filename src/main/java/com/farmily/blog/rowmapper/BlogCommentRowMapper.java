package com.farmily.blog.rowmapper;

import com.farmily.blog.contstant.BlogStatus;
import com.farmily.blog.model.BlogComment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BlogCommentRowMapper implements RowMapper<BlogComment> {

    @Override
    public BlogComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        BlogComment blogComment = new BlogComment();
        blogComment.setCommentId(rs.getInt("comment_id"));
        blogComment.setBlogId(rs.getInt("blog_id"));
        blogComment.setUserId(rs.getInt("user_id"));
        blogComment.setCommentTime(rs.getTimestamp("comment_time"));
        blogComment.setCommentPost(rs.getString("comment_post"));
        blogComment.setCommentLike(rs.getInt("comment_like"));
        blogComment.setCommentStatus( BlogStatus.valueOf(rs.getString("comment_status")));
        return blogComment;
    }
}
