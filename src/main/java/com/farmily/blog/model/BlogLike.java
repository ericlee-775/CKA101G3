package com.farmily.blog.model;

import com.farmily.blog.dto.BlogLikeId;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "blog_like")
@IdClass(BlogLikeId.class)
public class BlogLike {
    @Id
    @Column(name = "blog_id")
    private Integer blogId;
    @Id
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "create_at")
    private Timestamp createDate;



    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }
}
