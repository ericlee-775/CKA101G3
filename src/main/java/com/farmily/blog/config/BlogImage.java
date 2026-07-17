package com.farmily.blog.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BlogImage implements CommandLineRunner {//CommandLineRunner spring boot啟動完成後會自動呼叫它一次

    private final JdbcTemplate jdbc; //使用JdbcTemplate 下 SQL。

    public BlogImage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        int [] blogIds = {9001, 9002, 9003, 9004, 9005, 9006,
                          9007, 9008, 9009, 9010, 9011, 9012, 9013, 9014, 9015, 9016};
        for (int blogId : blogIds) {
            try {
                Integer len = jdbc.query(
                        "SELECT LENGTH(blog_img) FROM blog WHERE blog_id = ?",
                        rs -> rs.next() ? rs.getInt(1) : null,
                        blogId);

                if (len != null && len > 0) continue;

                ClassPathResource res = new ClassPathResource("sql/blog/images/" + blogId + ".jpg");
                if (!res.exists()) continue;                 // 沒這張檔就跳過
                byte[] bytes = res.getInputStream().readAllBytes();

                jdbc.update(
                        "UPDATE blog SET blog_img = ? WHERE blog_id = ? AND (blog_img IS NULL OR LENGTH(blog_img) = 0)",
                        bytes, blogId);

            } catch (Exception e) {

            }
        }
        //Blog_photo 照片上傳：blog 9003 的 3 張相簿照片（跟封面無關，放在迴圈外只跑一次）
        try {
            // 先看 9003 有沒有「真的有內容」的相簿照片，有就跳過（冪等）
            Integer photoCount = jdbc.query(
                    "SELECT COUNT(*) FROM blog_photo WHERE blog_id = 9003 AND blog_photo IS NOT NULL",
                    rs -> rs.next() ? rs.getInt(1) : 0);

            if (photoCount == null || photoCount == 0) {
                // 清掉種子資料那筆 NULL 佔位，避免前台出現空圖格
                jdbc.update("DELETE FROM blog_photo WHERE blog_id = 9003 AND blog_photo IS NULL");

                // 逐張讀檔，各 INSERT 一筆（blog_photo_id 是 AUTO_INCREMENT，不用自己給）
                String[] files = {"9003_1.jpg", "9003_2.jpg", "9003_3.jpg"};
                for (String f : files) {
                    ClassPathResource res = new ClassPathResource("sql/blog/photos/" + f);
                    if (!res.exists()) continue;
                    byte[] bytes = res.getInputStream().readAllBytes();
                    jdbc.update("INSERT INTO blog_photo (blog_id, blog_photo) VALUES (9003, ?)", bytes);
                }
            }
        } catch (Exception e) {
            // 相簿塞不進不擋啟動
        }
    }
}
