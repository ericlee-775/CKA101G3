package com.farmily.blog.repository;

import com.farmily.blog.model.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 留言的 JPA Repository。
 */
public interface BlogCommentRepository extends JpaRepository<BlogComment, Integer> {
}
