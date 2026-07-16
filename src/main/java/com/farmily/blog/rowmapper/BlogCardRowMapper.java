package com.farmily.blog.rowmapper;

import com.farmily.blog.constant.BlogStatus;
import com.farmily.blog.model.Blog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

// 只給「文章列表卡片」用：跟 BlogRowMapper 一樣，但多帶作者顯示名稱
// （需搭配 getBlogs 的 SQL 額外 SELECT 出 author_name、author_type 兩欄）
public class BlogCardRowMapper implements RowMapper<Blog> {

    @Override
    public Blog mapRow(ResultSet rs, int rowNum) throws SQLException {
        Blog blog = new Blog();
        blog.setBlogId(rs.getInt("blog_id"));
        blog.setBlogTitle(rs.getString("blog_title"));
        blog.setUserId(rs.getInt("user_id"));
        blog.setFarmerId(rs.getInt("farmer_id"));
        blog.setBlogTypeId(rs.getInt("blog_type_id"));
        blog.setBlogContent(rs.getString("blog_content"));
        blog.setBlogImg(rs.getBytes("blog_img"));
        blog.setBlogLikeCount(rs.getInt("blog_like_count"));
        blog.setBlogTime(rs.getTimestamp("blog_time"));
        blog.setBlogStatus(BlogStatus.valueOf(rs.getString("blog_status")));

        // 作者顯示資訊：小農文=農場名，會員文=暱稱/姓名（由 SQL 的 COALESCE 決定）
        blog.setAuthorName(rs.getString("author_name"));
        blog.setAuthorType(rs.getString("author_type"));

        return blog;
    }
}
