package com.farmily.news.repository;

import com.farmily.news.entity.NewStatus;
import com.farmily.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Integer> {
    Page<News> findByNewsStatus(NewStatus status, Pageable pageable);   // 公開：只 ACTIVE
    Optional<News> findByNewIdAndNewsStatus(Integer newId, NewStatus status); //看公開單筆

    List<News> findByNewsStatusAndPublishTimeLessThanEqual(NewStatus status, Timestamp time);
}
