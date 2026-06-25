package com.farmily;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 讓 Vue（前端路由 / SPA）部署在 /Rayrayvue/ 子路徑下也能正常運作。
 *
 * 解決兩個問題：
 *  ① 直接打 http://localhost:8080/Rayrayvue/ 不用再加 index.html
 *  ② 在 /Rayrayvue/products 等前端路由頁面按 F5 重新整理不會 404
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // ①-a 沒斜線的 /Rayrayvue → 導正成有斜線的 /Rayrayvue/
        //     （新版 Spring 把有/沒斜線視為不同網址，要分別處理）
        registry.addRedirectViewController("/Rayrayvue", "/Rayrayvue/");

        // ①-b 打 /Rayrayvue/ → 內部轉發到 index.html（網址列不變，使用者無感）
        registry.addViewController("/Rayrayvue/")
                .setViewName("forward:/Rayrayvue/index.html");

        // ② /Rayrayvue/ 底下「不含小數點」的網址（products、blogs… 這些前端路由）
        //    找不到實體檔案時 → 一律回 index.html，交給 Vue Router 自己決定顯示哪頁。
        //    用「不含 .」來排除 index-xxx.js、favicon.ico 這類真實檔案，讓它們照常載入。
        registry.addViewController("/Rayrayvue/{path:[^\\.]*}")
                .setViewName("forward:/Rayrayvue/index.html");
    }
}
