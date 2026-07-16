package com.farmily.blog.model;

import com.farmily.blog.constant.BlogStatus;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "blog_comment")
public class BlogComment {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //告訴JPA 這個 id 由資料庫自動產生
    @Column(name = "comment_id", updatable = false)
    private Integer commentId;

    @Column(name = "blog_id")
    private Integer blogId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "comment_time")
    private Timestamp commentTime;

    @Column(name = "comment_post")
    private String commentPost;

    @Column(name = "comment_like")
    private Integer commentLike;


    @Enumerated(EnumType.STRING)
    @Column(name = "comment_status")
    private BlogStatus commentStatus ;

    @Transient   // @Transient = 這欄不對應資料表欄位，JPA 不會拿去存/建表，只是用來裝查詢帶回的顯示值
    private String authorName;

    @Transient
    private String authorType;


    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
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

    public Timestamp getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Timestamp commentTime) {
        this.commentTime = commentTime;
    }

    public String getCommentPost() {
        return commentPost;
    }

    public void setCommentPost(String commentPost) {
        this.commentPost = commentPost;
    }

    public Integer getCommentLike() {
        return commentLike;
    }

    public void setCommentLike(Integer commentLike) {
        this.commentLike = commentLike;
    }

    public BlogStatus getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(BlogStatus commentStatus) {
        this.commentStatus = commentStatus;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorType() {
        return authorType;
    }

    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }
}
