package com.heima.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heima.admin.service.IReviewCrawlerArticleService;
import com.heima.common.aliyun.AliyunImageScanRequest;
import com.heima.common.aliyun.AliyunTextScanRequest;
import com.heima.common.common.pojo.EsIndexEntity;
import com.heima.common.constans.AliyunScanConstans;
import com.heima.common.constans.AppConstans;
import com.heima.common.constans.ESIndexConstants;
import com.heima.common.constans.WmMediaConstans;
import com.heima.common.kafka.KafkaSender;
import com.heima.common.kafka.messages.ArticleReviewSuccessMessage;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.pojos.*;
import com.heima.model.crawler.pojos.ClNews;
import com.heima.model.mappers.admin.AdChannelMapper;
import com.heima.model.mappers.app.*;
import com.heima.model.mappers.crawerls.ClNewsMapper;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.media.pojos.WmUser;
import com.heima.model.mess.ArticleReviewSuccessDto;
import com.heima.utils.common.Compute;
import com.heima.utils.common.ZipUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: HuangMuChen
 * @date: 2021/3/5 21:57
 * @version: V1.0
 * @Description: 爬虫文章审核业务层实现类
 */
@Service
@Slf4j
public class ReviewCrawlerArticleServiceImpl implements IReviewCrawlerArticleService {
    @Autowired
    private AliyunTextScanRequest aliyunTextScanRequest;
    @Autowired
    private AliyunImageScanRequest aliyunImageScanRequest;
    @Autowired
    private JestClient jestClient;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ApAuthorMapper apAuthorMapper;
    @Autowired
    private AdChannelMapper adChannelMapper;
    @Autowired
    private ClNewsMapper clNewsMapper;
    @Autowired
    private ApArticleLabelMapper apArticleLabelMapper;
    @Autowired
    private KafkaSender kafkaSender;

    /**
     * 定时任务：定时审核数据库状态为1(提交-待审核)的文章
     */
    @Override
    public void autoReviewCrawlerArticle() {
        // 调用mapper层查询所有状态为1的爬虫文章
        List<ClNews> clNewsList = this.clNewsMapper.selectList(new ClNews(null, (byte) 1));
        // 判断
        if (!CollectionUtils.isEmpty(clNewsList)) {
            log.info("自动审核定时任务检索出待审核爬虫文章数量：{}", clNewsList.size());
            // 循环遍历
            clNewsList.forEach(this::autoReviewCrawlerArticle);
        } else {
            log.info("自动审核定时任务未检索出待审核爬虫文章");
        }
    }

    /**
     * 根据爬虫文章id自动审核爬虫文章
     *
     * @param clNewsId 爬虫文章id
     */
    @Override
    public void autoReviewCrawlerArticle(Integer clNewsId) {
        // 调用mapper层进行查询(根据文章id、文章状态)
        ClNews clNews = this.clNewsMapper.selectByIdAndStatus(new ClNews(clNewsId, (byte) 1));
        // 判断
        if (clNews != null) {
            this.autoReviewCrawlerArticle(clNews);
        }
    }

    /**
     * 自动审核爬虫文章
     *
     * @param clNews 爬虫文章
     */
    @Override
    public void autoReviewCrawlerArticle(ClNews clNews) {
        // 当前时间
        long currentTime = System.currentTimeMillis();
        // 记录日志，方便后期排查问题
        log.info("开始自动审核流程");
        // 判断
        if (clNews != null && clNews.getStatus() == (byte) 1) {
            // 获取文章内容(未加密)和标题
            String content = clNews.getUnCompressContent();
            String title = clNews.getTitle();
            // 非空判断
            if (content == null || title == null) {
                // 修改爬虫文章状态为2 ->审核失败
                updateClNews(clNews, (byte) 2, "文章内容或标题为空");
                return;
            }
            // 计算匹配度 double semblance = SimHashUtils.getSemblance(title, content, 64);
            double similarDegree = Compute.SimilarDegree(content, title);
            // 0.12 ->代表匹配度为12%
            if (similarDegree <= 0) {
                // 修改自媒体文章状态为2 ->审核失败
                updateClNews(clNews, (byte) 2, "文章标题与内容不匹配");
                return;
            }
            List<String> imageList = new ArrayList<>();
            StringBuffer contentStr = new StringBuffer();
            // 先将JSON字符串转成JSON数组
            JSONArray jsonArray = JSON.parseArray(content);
            // 解析文章内容中的文本和图片
            analysisTextAndImages(imageList, contentStr, jsonArray);
            /*try {
                // 审核文本
                String textResponse = this.aliyunTextScanRequest.textScanRequest(contentStr.toString());
                // 判断审核结果
                if (StringUtils.isEmpty(textResponse) || !textResponse.equals("pass")) {
                    // 修改爬虫文章状态为2 ->审核失败
                    updateClNews(clNews, (byte) 2, "文本内容审核失败");
                    return;
                }
                String imageResponse = this.aliyunImageScanRequest.imageScanRequest(imageList);
                // 审核图片
                if (StringUtils.isEmpty(imageResponse) || !imageResponse.equals("pass")) {
                    // 修改爬虫文章状态为2 ->审核失败
                    updateClNews(clNews, (byte) 2, "图片内容审核失败");
                    return;
                }
            } catch (Exception e) {
                log.error("爬虫文章：{}自动审核过程中出现异常：{}", clNews.getTitle(), e.getMessage());
                return;
            }*/
            // 立即发布
            reviewSuccessSaveAll(clNews, imageList);
        }
        // 记录日志，方便后期排查问题
        log.info("审核流程结束，耗时：{}", System.currentTimeMillis() - currentTime);
    }

    /**
     * 更新爬虫文章信息
     *
     * @param clNews
     * @param status
     * @param message
     */
    private void updateClNews(ClNews clNews, byte status, String message) {
        clNews.setStatus(status);
        clNews.setReason(message);
        this.clNewsMapper.updateStatus(clNews);
    }

    /**
     * 再将数组中的每个元素转成JSON对象
     *
     * @param imageList
     * @param contentStr
     * @param jsonArray
     */
    private void analysisTextAndImages(List<String> imageList, StringBuffer contentStr, JSONArray jsonArray) {
        // 非空判断
        if (jsonArray != null) {
            // 遍历JSON数组
            jsonArray.forEach(obj -> {
                // 将JSON数组中的每个元素转成JSON对象
                JSONObject jsonObject = (JSONObject) obj;
                // 类型
                String type = jsonObject.getString("type");
                // 值
                String value = jsonObject.getString("value");
                // 取出图片
                if ("image".equals(type)) {
                    imageList.add(value);
                }
                // 取出文本
                if ("text".equals(type)) {
                    contentStr.append(value);
                }
            });
        }
    }

    /**
     * 审核通过后保存相应数据
     *
     * @param clNews
     */
    private void reviewSuccessSaveAll(ClNews clNews, List<String> imageList) {
        // 1. 保存作者信息
        ApAuthor apAuthor = this.apAuthorMapper.selectByAuthorName(clNews.getName());
        // 如果不存在作者信息，则需要保存作者信息
        if (apAuthor == null || apAuthor.getId() == null) {
            apAuthor = new ApAuthor();
            apAuthor.setCreatedTime(new Date());
            apAuthor.setType(AppConstans.AP_AUTHOR_TYPE_CRAWLER);
            apAuthor.setName(clNews.getName());
            // 调用mapper层进行保存
            this.apAuthorMapper.insert(apAuthor);
        }

        // 2. 保存文章基本信息
        ApArticle apArticle = new ApArticle();
        AdChannel adChannel = this.adChannelMapper.selectByPrimaryKey(clNews.getChannelId());
        if (adChannel != null) {
            apArticle.setChannelId(adChannel.getId());
            apArticle.setChannelName(adChannel.getName());
        }
        apArticle.setAuthorId(apAuthor.getId().longValue());
        apArticle.setAuthorName(apAuthor.getName());
        apArticle.setCreatedTime(new Date());
        apArticle.setTitle(clNews.getTitle());
        apArticle.setLayout((short) clNews.getType());
        apArticle.setOrigin(false);
        apArticle.setPublishTime(new Date());
        apArticle.setLabels(clNews.getLabelIds());
        // 封装图片
        if (!CollectionUtils.isEmpty(imageList)) {
            StringBuilder sb = new StringBuilder();
            // 最多截取三张图片
            for (int i = 0; i < imageList.size() && i < 3; i++) {
                if (sb.length() > 0) {
                    sb.append(WmMediaConstans.WM_NEWS_IMAGES_SWPARATOR);
                }
                sb.append(imageList.get(i));
            }
            apArticle.setImages(sb.toString());
        }
        // 调用mapper层进行保存
        this.apArticleMapper.insert(apArticle);

        // 3. 保存文章标签【一篇文章可能对应多个标签：java、springboot】
        if (StringUtils.isNotEmpty(apArticle.getLabels())) {
            // 按逗号切割
            String[] labelIdArray = apArticle.getLabels().split(",");
            // 循环数组
            for (String labelId : labelIdArray) {
                // 查询数据库
                List<ApArticleLabel> apArticleLabelList = this.apArticleLabelMapper.selectList(new ApArticleLabel(apArticle.getId(), Integer.parseInt(labelId)));
                // 判断
                if (!CollectionUtils.isEmpty(apArticleLabelList)) {
                    ApArticleLabel apArticleLabel = apArticleLabelList.get(0);
                    // 次数加1
                    apArticleLabel.setCount(apArticleLabel.getCount() + 1);
                    // 更新数据库
                    this.apArticleLabelMapper.updateByPrimaryKeySelective(apArticleLabel);
                } else {
                    ApArticleLabel apArticleLabel = new ApArticleLabel(apArticle.getId(), Integer.parseInt(labelId));
                    // 统计次数
                    apArticleLabel.setCount(1);
                    // 调用mapper层进行保存
                    this.apArticleLabelMapper.insert(apArticleLabel);
                }
            }
        }

        // 4. 保存文章内容
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        apArticleContent.setContent(clNews.getContent());
        // 调用mapper层进行保存
        this.apArticleContentMapper.insert(apArticleContent);

        // 5. 保存文章配置信息
        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        apArticleConfig.setIsComment(true);
        apArticleConfig.setIsDelete(false);
        apArticleConfig.setIsDown(false);
        apArticleConfig.setIsForward(true);
        // 调用mapper层进行保存
        this.apArticleConfigMapper.insert(apArticleConfig);

        // 6. 创建ES索引
        EsIndexEntity entity = new EsIndexEntity();
        entity.setId(apArticle.getId().longValue());
        if (apArticle.getChannelId() != null) {
            entity.setChannelId(new Long(apArticle.getChannelId()));
        }
        entity.setContent(clNews.getUnCompressContent());
        entity.setPublishTime(apArticle.getPublishTime());
        entity.setStatus(1L);
        entity.setTag("crawler");
        entity.setTitle(apArticle.getTitle());
        // new一个索引构造器
        Index index = new Index.Builder(entity)
                .id(apArticle.getId().toString())
                .refresh(true)
                .index(ESIndexConstants.ARTICLE_INDEX)
                .type(ESIndexConstants.DEFAULT_DOC)
                .build();
        JestResult result = null;
        // 执行插入
        try {
            result = this.jestClient.execute(index);
        } catch (IOException e) {
            log.error("执行ES创建索引失败，message:{}", e.getMessage());
        }
        // 判断插入结果
        if (result != null && !result.isSucceeded()) {
            log.error("插入更新索引失败：message:{}", result.getErrorMessage());
        }

        // 7. 修改wmNews的状态为9
        clNews.setArticleId(apArticle.getId());
        updateClNews(clNews, (byte) 9, "审核成功");

        // 8. 通知文章迁移微服务，将审核通过的文章同步到hbase中
        ArticleReviewSuccessDto dto = new ArticleReviewSuccessDto();
        dto.setArticleId(apArticle.getId());
        dto.setType(ArticleReviewSuccessDto.ArticleReviewSuccessType.CRAWLER);
        this.kafkaSender.sendArticleReviewSuccessMessage(new ArticleReviewSuccessMessage(dto));
    }
}
