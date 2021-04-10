package com.heima.admin.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.admin.service.IReviewCrawlerArticleService;
import com.heima.admin.service.IReviewMediaArticleService;
import com.heima.common.kafka.KafkaListener;
import com.heima.common.kafka.KafkaTopicConfig;
import com.heima.common.kafka.messages.AutoReviewArticleMessage;
import com.heima.model.mess.AutoReviewArticleDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: HuangMuChen
 * @date: 2021/2/22 19:48
 * @version: V1.0
 * @Description: 监听文章自动审核消息
 */
@Component
@Slf4j
public class AutoReviewArticleListener implements KafkaListener<String, String> {
    @Autowired
    private KafkaTopicConfig kafkaTopicConfig;
    @Autowired
    private IReviewMediaArticleService reviewMediaArticleService;
    @Autowired
    private IReviewCrawlerArticleService reviewCrawlerArticleService;
    @Autowired
    private ObjectMapper mapper;

    /**
     * 指定监听的消息主题
     *
     * @return
     */
    @Override
    public String topic() {
        return this.kafkaTopicConfig.getAutoReviewArticle();
    }

    /**
     * 处理消息
     *
     * @param data
     * @param consumer
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Consumer<?, ?> consumer) {
        // 获取监听到的消息
        String value = data.value();
        // 记录日志
        log.info("接收到的消息为：{}", value);

        try {
            // 将字符串消息转为对象
            AutoReviewArticleMessage message = this.mapper.readValue(value, AutoReviewArticleMessage.class);
            // 非空判断
            if (message != null) {
                // 获取文章类型
                AutoReviewArticleDto.AutoReviewArticleType type = message.getData().getType();
                // 根据文章类型选择不同审核方法
                if (AutoReviewArticleDto.AutoReviewArticleType.WEMEDIA.equals(type)) {
                    // 审核自媒体文章
                    Integer articleId = message.getData().getArticleId();
                    // 非空判断
                    if (articleId != null) {
                        this.reviewMediaArticleService.autoReviewMediaArticle(articleId);
                    }
                } else if (AutoReviewArticleDto.AutoReviewArticleType.CRAWLER.equals(type)) {
                    // 审核爬虫文章
                    Integer articleId = message.getData().getArticleId();
                    // 非空判断
                    if (articleId != null) {
                        this.reviewCrawlerArticleService.autoReviewCrawlerArticle(articleId);
                    }
                }
            }
        } catch (Exception e) {
            // 记录日志
            log.error("自动审核文章错误:[{}],{}", value, e.getMessage());
            // 抛出异常
            throw new RuntimeException("WS消息处理错误", e);
        }
    }
}
