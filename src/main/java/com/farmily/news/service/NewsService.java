package com.farmily.news.service;

import com.farmily.blog.util.Page;
import com.farmily.news.dto.NewsRequest;
import com.farmily.news.dto.NewsResponse;


public interface NewsService {

    //公開
    byte[] getNewsImage(Integer newsId); // 封面圖

    Page<NewsResponse> getPublishedNews(Integer offset, Integer limit);        // 公開列表(ACTIVE)

    NewsResponse getPublishedNewsOne(Integer newsId);                          // 公開單筆(ACTIVE)

    //管理員

    byte[] getAdminNewsImage(Integer newsId);                                  // admin 封面圖(任意狀態)

    Page<NewsResponse> getAllNews(Integer offset, Integer limit);              // admin 全狀態列表

    NewsResponse getNewsOne(Integer newsId);                                   // admin 單筆(任意狀態)

    NewsResponse createNews(Integer adminId, NewsRequest request);             // 新增

    NewsResponse updateNews(Integer newsId, NewsRequest request);              // 修改

    NewsResponse changeNewsStatus(Integer newsId, Boolean publish);            // 發布/下架

    void deleteNews(Integer newsId);                                           // 刪除
}

