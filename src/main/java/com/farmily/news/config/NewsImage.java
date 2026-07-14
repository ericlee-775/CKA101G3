package com.farmily.news.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewsImage implements CommandLineRunner {

    private JdbcTemplate jdbc;
    public NewsImage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        int[] newsIds = {1, 2, 3, 4, 5};

        for (int newsId : newsIds) {
            try {
                // 已經有圖就跳過（冪等，重啟不會重塞）
                Integer len = jdbc.query(
                        "SELECT LENGTH(cover_image) FROM news WHERE news_id = ?",
                        rs -> rs.next() ? rs.getInt(1) : null,
                        newsId);
                if (len != null && len > 0) continue;

                // 讀 classpath 的圖：sql/news/1.jpg ... 5.jpg
                ClassPathResource res = new ClassPathResource("sql/news/" + newsId + ".jpg");
                if (!res.exists()) continue;
                byte[] bytes = res.getInputStream().readAllBytes();

                // 只在 cover_image 為空時更新
                jdbc.update(
                        "UPDATE news SET cover_image = ? WHERE news_id = ? AND (cover_image IS NULL OR LENGTH(cover_image) = 0)",
                        bytes, newsId);
            } catch (Exception e) {
                System.err.println("News 圖片 seeder 失敗 newsId=" + newsId + ": " + e.getMessage());
            }
        }
    }
}
