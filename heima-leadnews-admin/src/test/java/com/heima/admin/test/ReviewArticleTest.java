package com.heima.admin.test;

import com.heima.AdminApplication;
import com.heima.admin.service.IReviewMediaArticleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: HuangMuChen
 * @date: 2021/2/22 19:20
 * @version: V1.0
 * @Description: 文章审核测试
 */
@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class ReviewArticleTest {
    @Autowired
    private IReviewMediaArticleService reviewMediaArticleService;

    /**
     * 自媒体文章审核测试
     */
    @Test
    public void reviewMediaArticleTest() {
        this.reviewMediaArticleService.autoReviewMediaArticle(5108);
    }

    /**
     * 爬虫文章审核测试
     */
    @Test
    public void reviewCrawlerArticleTest() {
        this.reviewMediaArticleService.autoReviewMediaArticle(5108);
    }
}
