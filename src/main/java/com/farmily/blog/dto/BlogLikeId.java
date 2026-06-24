package com.farmily.blog.dto;

import java.io.Serializable;
import java.util.Objects;

public class BlogLikeId implements Serializable {
    private Integer blogId;
    private Integer userId;

    public BlogLikeId() {
    }

    public BlogLikeId(Integer blogId, Integer userId) {
        this.blogId = blogId;
        this.userId = userId;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlogLikeId other)) return false;

        return Objects.equals(blogId, other.blogId)
                && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blogId, userId);
    }

}
