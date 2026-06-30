package com.farmily.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "blog_photo")
public class BlogPhoto {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_photo_id")
    private Integer blogPhotoId;

    @Column(name = "blog_id")
    private Integer blogId;

    @Column(name = "blog_photo")
    private byte[] blogPhoto;

    public Integer getBlogPhotoId() {
        return blogPhotoId;
    }

    public void setBlogPhotoId(Integer blogPhotoId) {
        this.blogPhotoId = blogPhotoId;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    @JsonIgnore
    public byte[] getPhoto() {
        return blogPhoto;
    }

    public void setPhoto(byte[] photo) {
        this.blogPhoto = photo;
    }
}
