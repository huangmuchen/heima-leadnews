package com.heima.admin.job;

import com.heima.admin.service.IReviewCrawlerArticleService;
import com.heima.common.quartz.AbstractJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: HuangMuChen
 * @date: 2021/3/5 21:50
 * @version: V1.0
 * @Description: 定时审核爬虫文章
 */
@Component
@DisallowConcurrentExecution
@Slf4j
public class AutoReviewCrawlerArticleQuartz extends AbstractJob {
    @Autowired
    private IReviewCrawlerArticleService reviewCrawlerArticleService;

    @Override
    public String[] triggerCron() {
        // 0 0 23 * * ? -> 每天晚上23点执行
        return new String[]{"0 0 23 * * ?"};
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        long currentTime = System.currentTimeMillis();
        log.info("开始定时任务：自动审核爬虫文章");
        this.reviewCrawlerArticleService.autoReviewCrawlerArticle();
        log.info("定时任务：自动审核爬虫文章,执行完成,耗时：{}", System.currentTimeMillis() - currentTime);
    }

    @Override
    public String descTrigger() {
        return "每天晚上23点执行";
    }
}
