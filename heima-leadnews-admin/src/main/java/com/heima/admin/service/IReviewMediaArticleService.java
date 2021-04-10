package com.heima.admin.service;

/**
 * @author: HuangMuChen
 * @date: 2021/2/20 19:17
 * @version: V1.0
 * @Description: 自媒体文章审核业务层接口
 */
public interface IReviewMediaArticleService {

    /**
     * 自动审核自媒体文章
     *
     * @param wmNewsId 自媒体文章id
     */
    void autoReviewMediaArticle(Integer wmNewsId);
}
