package com.farmily.blog.repository;

import com.farmily.blog.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文章的 JPA Repository。
 * 只要繼承 JpaRepository<實體型別, 主鍵型別>，
 * Spring Data 就自動提供 findById / findAll / save / delete 等方法，不用寫任何實作。
 */
public interface BlogRepository extends JpaRepository<Blog, Integer> {
}
