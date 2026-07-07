package com.farmily;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * 讓 Vue（前端路由 / SPA）部署在 /farmily-web/ 子路徑下也能正常運作。
 *
 * 解決兩個問題：
 *  ① 直接打 http://localhost:8080/farmily-web/ 不用再加 index.html
 *  ② 在 /farmily-web/... 底下任意深度的前端路由頁面（例如 /products、
 *     /products/123）直接輸入網址或按 F5 重整，都不會 404。
 *
 * 核心觀念就是「SPA fallback」，跟 Nginx 的 try_files $uri /index.html 一樣：
 *   實體檔案存在（js/css/圖片）→ 照常回傳；
 *   找不到（那就是前端路由）  → 一律回 index.html，交給 Vue Router 決定顯示哪頁
 *                              （對不到就命中 Vue 的 404 頁）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // ①-a 沒斜線的 /farmily-web → 導正成有斜線的 /farmily-web/
        //     （新版 Spring 把有/沒斜線視為不同網址，要分別處理）
        registry.addRedirectViewController("/farmily-web", "/farmily-web/");

        // ①-b 打 /farmily-web/ → 內部轉發到 index.html（網址列不變，使用者無感）
        registry.addViewController("/farmily-web/")
                .setViewName("forward:/farmily-web/index.html");
    }

    /**
     * ② 任意深度的 SPA fallback。
     *
     * 用自訂的 PathResourceResolver 取代舊的 addViewController("{path:[^.]*}")：
     * 舊寫法用正規表達式排除含「.」的檔案，但一個 {path} 只吃單層路徑，
     * 兩層以上（/products/123）就漏接。這裡改用「檔案存在與否」來判斷，
     * 天生就涵蓋任意深度，也不必猜哪些是檔案、哪些是路由。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/farmily-web/**")
                .addResourceLocations("classpath:/static/farmily-web/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        // 實體檔案存在且可讀（index-xxx.js、favicon.ico、圖片…）→ 照常回傳
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // 否則視為前端路由（任意深度）→ 一律回 index.html，交給 Vue Router
                        return location.createRelative("index.html");
                    }
                });
    }
}
