package com.farmily.news.service.impl;

import com.farmily.blog.util.Page;
import com.farmily.news.dto.NewsRequest;
import com.farmily.news.dto.NewsResponse;
import com.farmily.news.entity.NewStatus;
import com.farmily.news.entity.News;
import com.farmily.news.repository.NewsRepository;
import com.farmily.news.service.NewsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }


    //取得封面圖 bytes
    @Override
    @Transactional(readOnly = true)
    public byte[] getNewsImage(Integer newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在: " + newsId));
        return news.getCoverImg();
    }
    //公開
    @Override
    @Transactional(readOnly = true)
    public Page<NewsResponse> getPublishedNews(Integer offset, Integer limit) {
        //頁碼：offset / limit    每頁筆數：limit     排序：Sort.by("publishTime")
        Pageable pageable = PageRequest.of(offset / limit, limit , Sort.by("publishTime").descending());
        //spring data 的 Page
        org.springframework.data.domain.Page<News> data =
                newsRepository.findByNewsStatus(NewStatus.VISIBLE, pageable);

        List<NewsResponse> results = new ArrayList<>();
        for (News n : data.getContent()) {
            results.add(NewsResponse.from(n));
        }

        Page<NewsResponse> page = new Page<>();   // 這個是 blog.util.Page
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal((int) data.getTotalElements());
        page.setResults(results);
        return page;
    }
    //公開
    @Override
    @Transactional(readOnly = true)
    public NewsResponse getPublishedNewsOne(Integer newsId) {
        News news = newsRepository.findByNewIdAndNewsStatus(newsId, NewStatus.VISIBLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在: " + newsId));

        return NewsResponse.from(news);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsResponse> getAllNews(Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                Sort.by("createdAt").descending());

        org.springframework.data.domain.Page<News> data = newsRepository.findAll(pageable);

        List<NewsResponse> results = new ArrayList<>();
        for (News n : data.getContent()) {
            results.add(NewsResponse.from(n));
        }

        Page<NewsResponse> page = new Page<>();   // blog.util.Page
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal((int) data.getTotalElements());
        page.setResults(results);
        return page;

    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponse getNewsOne(Integer newsId) {

       News news = newsRepository.findById(newsId)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在：" + newsId));
        return NewsResponse.from(news);
    }

    //創建最新消息
    @Override
    public NewsResponse createNews(Integer adminId, NewsRequest request) {

        News news = new News();
        news.setAdminId(adminId);
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setCoverImg(request.getCoverImg());
        news.setPublishTime(request.getPublishTime());
        news.setNewsStatus(NewStatus.DRAFT);

        News saved = newsRepository.save(news);

        return NewsResponse.from(saved);
    }

    @Override
    public NewsResponse updateNews(Integer newsId, NewsRequest request) {
        //撈出資料庫那一筆，沒有就404
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在：" + newsId));

        //改欄位
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setPublishTime(request.getPublishTime());
        //圖片 沒有新圖 就回傳舊圖
        if (request.getCoverImg() != null) {
            news.setCoverImg(request.getCoverImg());
        }
        //存回，回傳更新的DTO
        News saved = newsRepository.save(news);
        return NewsResponse.from(saved);
    }

    @Override
    public NewsResponse changeNewsStatus(Integer newsId, Boolean publish) {
        //撈資料
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在：" + newsId));

        if(publish) {
            //發布：上架
            news.setNewsStatus(NewStatus.VISIBLE);
            //若沒設定發布時間，補上 現在
            if (news.getPublishTime() == null) {
                news.setPublishTime(Timestamp.valueOf(LocalDateTime.now().withNano(0)));
            }

        }else {
            //下架
            news.setNewsStatus(NewStatus.HIDDEN);
        }

        return NewsResponse.from(newsRepository.save(news));
    }

    @Override
    public void deleteNews(Integer newsId) {

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "最新消息不存在：" + newsId));
        //刪除
        newsRepository.delete(news);
    }

    //排程方法
    @Scheduled(fixedRate = 60000)   // 每 60000 毫秒（=60 秒）跑一次
    public void publishDueNews() {

        // 現在時間（乾淨到秒）
        Timestamp now = Timestamp.valueOf(LocalDateTime.now().withNano(0));

        // 撈出「該上架的」：DRAFT 且發布時間已過
        List<News> dueList = newsRepository.findByNewsStatusAndPublishTimeLessThanEqual(NewStatus.DRAFT, now);

        // 逐筆改成 VISIBLE
        for (News news : dueList) {
            news.setNewsStatus(NewStatus.VISIBLE);
        }

        // 存回
        newsRepository.saveAll(dueList);
    }
}
