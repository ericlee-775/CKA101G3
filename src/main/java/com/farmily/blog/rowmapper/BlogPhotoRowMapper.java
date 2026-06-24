package com.farmily.blog.rowmapper;

import com.farmily.blog.model.BlogPhoto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BlogPhotoRowMapper implements RowMapper<BlogPhoto> {

    @Override
    public BlogPhoto mapRow(ResultSet rs, int rowNum) throws SQLException {
        BlogPhoto blogPhoto = new BlogPhoto();
        blogPhoto.setBlogPhotoId(rs.getInt("blog_photo_id"));
        blogPhoto.setBlogId(rs.getInt("blog_id"));

        return blogPhoto;
    }
}
