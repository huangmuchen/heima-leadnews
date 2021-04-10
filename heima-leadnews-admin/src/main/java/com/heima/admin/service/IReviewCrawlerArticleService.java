package com.heima.admin.service;

import com.heima.model.crawler.pojos.ClNews;

/**
 * @author: HuangMuChen
 * @date: 2021/3/5 21:39
 * @version: V1.0
 * @Description: 爬虫文章审核业务层接口
 */
public interface IReviewCrawlerArticleService {

    /**
     * 定时任务：定时审核数据库状态为1(提交-待审核)的文章
     */
    void autoReviewCrawlerArticle();

    /**
     * 根据爬虫文章id自动审核爬虫文章
     *
     * @param clNewsId 爬虫文章id
     */
    void autoReviewCrawlerArticle(Integer clNewsId);

    /**
     * 自动审核爬虫文章
     *
     * @param clNews 爬虫文章
     */
    void autoReviewCrawlerArticle(ClNews clNews);
}
