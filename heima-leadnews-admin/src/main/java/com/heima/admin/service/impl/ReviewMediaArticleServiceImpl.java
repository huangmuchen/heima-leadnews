package com.heima.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heima.admin.service.IReviewMediaArticleService;
import com.heima.common.aliyun.AliyunImageScanRequest;
import com.heima.common.aliyun.AliyunTextScanRequest;
import com.heima.common.common.pojo.EsIndexEntity;
import com.heima.common.constans.AppConstans;
import com.heima.common.constans.ESIndexConstants;
import com.heima.common.constans.WmMediaConstans;
import com.heima.common.kafka.KafkaSender;
import com.heima.common.kafka.messages.ArticleReviewSuccessMessage;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.mappers.admin.AdChannelMapper;
import com.heima.model.mappers.app.*;
import com.heima.model.mappers.wemedia.WmNewsMapper;
import com.heima.model.mappers.wemedia.WmUserMapper;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.media.pojos.WmUser;
import com.heima.model.mess.ArticleReviewSuccessDto;
import com.heima.model.user.pojos.ApUserMessage;
import com.heima.utils.common.Compute;
import com.heima.utils.common.ZipUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: HuangMuChen
 * @date: 2021/2/20 19:21
 * @version: V1.0
 * @Description: 自媒体文章审核业务层实现类
 */
@Service
@Slf4j
public class ReviewMediaArticleServiceImpl implements IReviewMediaArticleService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private AliyunTextScanRequest aliyunTextScanRequest;
    @Autowired
    private AliyunImageScanRequest aliyunImageScanRequest;
    @Autowired
    private ApUserMessageMapper apUserMessageMapper;
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApAuthorMapper apAuthorMapper;
    @Autowired
    private AdChannelMapper adChannelMapper;
    @Autowired
    private JestClient jestClient;
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @Autowired
    private KafkaSender kafkaSender;

    /**
     * 自动审核自媒体文章：
     *
     * @param wmNewsId 自媒体文章id
     */
    @Override
    public void autoReviewMediaArticle(Integer wmNewsId) {
        // 获取自媒体文章信息
        WmNews wmNews = this.wmNewsMapper.selectNewsByPrimaryKey(wmNewsId);
        // 非空判断
        if (wmNews != null) {
            // 判断文章状态是否为4 ->人工审核通过
            if (WmMediaConstans.WM_NEWS_APPROVED_STATUS.equals(wmNews.getStatus())) {
                // 人工审核通过，则保存数据（ap_article_config   ap_article   ap_article_content  ap_author）
                reviewSuccessSaveAll(wmNews);
                return;
            }
            // 判断文章状态是否为8 ->审核通过（待发布） 且 发布时间 < 当前时间
            if (WmMediaConstans.WM_NEWS_AUTHED_STATUS.equals(wmNews.getStatus()) && wmNews.getPublishTime() != null && wmNews.getPublishTime().getTime() < System.currentTimeMillis()) {
                reviewSuccessSaveAll(wmNews);
                return;
            }
            // 文章状态为1 ->提交（待审核），开始审核文章
            if (WmMediaConstans.WM_NEWS_SUMMIT_STATUS.equals(wmNews.getStatus())) {
                // 审核标题与内容是否匹配
                String content = wmNews.getContent();
                String title = wmNews.getTitle();
                // 非空判断
                if (content == null || title == null) {
                    // 修改自媒体文章状态为2 ->审核失败
                    updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "文章内容或标题为空");
                    return;
                }
                // 计算匹配度 double semblance = SimHashUtils.getSemblance(title, content, 64);
                double similarDegree = Compute.SimilarDegree(content, title);
                // 0.12 ->代表匹配度为12%
                if (similarDegree <= 0) {
                    // 修改自媒体文章状态为2 ->审核失败
                    updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "文章标题与内容不匹配");
                    // 发送消息通知自媒体用户审核失败
                    saveApUserMessage(wmNews, AppConstans.REVIEW_FAILED, "审核失败,文章标题与内容不匹配");
                    return;
                }
                // "[{'type':'text','value':'好好读书'},{'type':'image','value':'http://192.168.222.131/group1/M00/00/01/wKjeg18CyryADV1jAAF86VDhMRw062.jpg'}]"
                List<String> imageList = new ArrayList<>();
                StringBuffer contentStr = new StringBuffer();
                // 先将JSON字符串转成JSON数组
                JSONArray jsonArray = JSON.parseArray(content);
                // 解析文章内容中的文本和图片
                analysisTextAndImages(imageList, contentStr, jsonArray);
                // 封面图片也需审核
                if (StringUtils.isNotEmpty(wmNews.getImages())) {
                    // group1/M00/00/01/wKjeg18OuGSARnLhAABfSfYfPJ4418.jpg,group1/M00/00/01/wKjeg17x__uAFpRpAADyLb0Fu9s561.jpg
                    String[] imageArray = wmNews.getImages().split(String.valueOf(WmMediaConstans.WM_NEWS_IMAGES_SWPARATOR));
                    // 遍历数组
                    for (String image : imageArray) {
                        imageList.add(this.FILE_SERVER_URL + image);
                    }
                }
                /*try {
                    // 审核文本
                    String textResponse = this.aliyunTextScanRequest.textScanRequest(contentStr.toString());
                    // 判断审核结果
                    if (StringUtils.isNotEmpty(textResponse)) {
                        if (AliyunScanConstans.ALIYUN_SCAN_REVIEW.equals(textResponse)) {
                            updateWmNews(wmNews, WmMediaConstans.WM_NEWS_MANUAL_STATUS, "需要人工审核");
                            return;
                        }
                        if (AliyunScanConstans.ALIYUN_SCAN_BLOCK.equals(textResponse)) {
                            // 修改自媒体文章状态为2 ->审核失败
                            updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "文本内容涉嫌违规");
                            // 发送消息通知自媒体用户审核失败
                            saveApUserMessage(wmNews, AppConstans.REVIEW_FAILED, "审核失败,文本内容涉嫌违规");
                            return;
                        }
                    } else {
                        // 修改自媒体文章状态为2 ->审核失败
                        updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "文本审核出现问题");
                        // 发送消息通知自媒体用户审核失败
                        saveApUserMessage(wmNews, AppConstans.REVIEW_FAILED, "审核失败,文本审核出现问题");
                        return;
                    }
                    // 审核图片
                    String imageResponse = this.aliyunImageScanRequest.imageScanRequest(imageList);
                    // 判断审核结果
                    if (StringUtils.isNotEmpty(imageResponse)) {
                        if (AliyunScanConstans.ALIYUN_SCAN_REVIEW.equals(imageResponse)) {
                            updateWmNews(wmNews, WmMediaConstans.WM_NEWS_MANUAL_STATUS, "需要人工审核");
                            return;
                        }
                        if (AliyunScanConstans.ALIYUN_SCAN_BLOCK.equals(imageResponse)) {
                            // 修改自媒体文章状态为2 ->审核失败
                            updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "图片内容涉嫌违规");
                            // 发送消息通知自媒体用户审核失败
                            saveApUserMessage(wmNews, AppConstans.REVIEW_FAILED, "审核失败,图片内容涉嫌违规");
                            return;
                        }
                    } else {
                        // 修改自媒体文章状态为2 ->审核失败
                        updateWmNews(wmNews, WmMediaConstans.WM_NEWS_FAILED_STATUS, "图片审核出现问题");
                        // 发送消息通知自媒体用户审核失败
                        saveApUserMessage(wmNews, AppConstans.REVIEW_FAILED, "审核失败,图片审核出现问题");
                        return;
                    }
                } catch (Exception e) {
                    log.error("自媒体文章：{}自动审核过程中出现异常：{}", wmNews.getTitle(), e.getMessage());
                    return;
                }*/
                // 判断发布时间是否大于当前时间
                if (wmNews.getPublishTime() != null) {
                    if (wmNews.getPublishTime().getTime() > System.currentTimeMillis()) {
                        // 修改自媒体文章状态为8 ->审核通过（待发布）
                        updateWmNews(wmNews, WmMediaConstans.WM_NEWS_AUTHED_STATUS, "待发布");
                    } else {
                        // 立即发布
                        reviewSuccessSaveAll(wmNews);
                    }
                } else {
                    // 如果自媒体用户没有指定发布时间，则立即发布
                    reviewSuccessSaveAll(wmNews);
                }
            }
        }
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
                if (WmMediaConstans.WM_NEWS_TYPE_IMAGE.equals(type)) {
                    imageList.add(value);
                }
                // 取出文本
                if (WmMediaConstans.WM_NEWS_TYPE_TEXT.equals(type)) {
                    contentStr.append(value);
                }
            });
        }
    }

    /**
     * 发送审核消息给自媒体用户
     *
     * @param wmNews
     * @param type    消息类型
     * @param message 消息内容
     */
    private void saveApUserMessage(WmNews wmNews, int type, String message) {
        ApUserMessage apUserMessage = new ApUserMessage();
        // 封装数据
        apUserMessage.setContent(message);
        apUserMessage.setType(type);
        apUserMessage.setCreatedTime(new Date());
        apUserMessage.setIsRead(false);
        apUserMessage.setUserId(wmNews.getUserId());
        // 调用mapper层保存数据
        this.apUserMessageMapper.insert(apUserMessage);
    }

    /**
     * 更新自媒体文章信息
     *
     * @param wmNews
     * @param status
     * @param message
     */
    private void updateWmNews(WmNews wmNews, short status, String message) {
        wmNews.setStatus(status);
        wmNews.setReason(message);
        this.wmNewsMapper.updateByPrimaryKeySelective(wmNews);
    }

    /**
     * 审核通过后保存相应数据
     *
     * @param wmNews
     */
    private void reviewSuccessSaveAll(WmNews wmNews) {
        // 保存作者信息
        ApAuthor apAuthor = null;
        if (wmNews.getUserId() != null) {
            // 查询作者名称
            WmUser wmUser = this.wmUserMapper.queryUserById(wmNews.getUserId());
            // 非空判断
            if (wmUser != null && StringUtils.isNotEmpty(wmUser.getName())) {
                // 根据作者名称查询作者表
                apAuthor = this.apAuthorMapper.selectByAuthorName(wmUser.getName());
                // 如果不存在作者信息，则需要保存作者信息
                if (apAuthor == null || apAuthor.getId() == null) {
                    apAuthor = new ApAuthor();
                    apAuthor.setUserId(wmUser.getApUserId());
                    apAuthor.setCreatedTime(new Date());
                    apAuthor.setType(AppConstans.AP_AUTHOR_TYPE_MEDIA);
                    apAuthor.setName(wmUser.getName());
                    apAuthor.setWmUserId(wmUser.getId());
                    this.apAuthorMapper.insert(apAuthor);
                }
            }
        }
        // 保存文章基本信息
        ApArticle apArticle = new ApArticle();
        // 添加作者信息
        if (apAuthor != null) {
            apArticle.setAuthorId(apAuthor.getId().longValue());
            apArticle.setAuthorName(apAuthor.getName());
        }
        // 添加频道信息
        AdChannel adChannel = this.adChannelMapper.selectByPrimaryKey(wmNews.getChannelId());
        // 判断
        if (adChannel != null) {
            apArticle.setChannelId(adChannel.getId());
            apArticle.setChannelName(adChannel.getName());
        }
        apArticle.setCreatedTime(new Date());
        apArticle.setTitle(wmNews.getTitle());
        apArticle.setLayout(wmNews.getType());
        if (wmNews.getPublishTime() != null) {
            apArticle.setPublishTime(wmNews.getPublishTime());
        } else {
            apArticle.setPublishTime(new Date());
        }
        apArticle.setOrigin(true);
        // 封装图片，需要全路径
        String images = wmNews.getImages();
        // 判断
        if (StringUtils.isNotEmpty(images)) {
            // 按','分割
            String[] imageArray = images.split(String.valueOf(WmMediaConstans.WM_NEWS_IMAGES_SWPARATOR));
            StringBuffer sb = new StringBuffer();
            // 拼接
            for (String url : imageArray) {
                if (sb.length() > 0) {
                    sb.append(WmMediaConstans.WM_NEWS_IMAGES_SWPARATOR);
                }
                sb.append(this.FILE_SERVER_URL).append(url);
            }
            apArticle.setImages(sb.toString());
        }
        // 调用mapper层进行保存
        this.apArticleMapper.insert(apArticle);

        // 保存文章内容
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        // 加密
        apArticleContent.setContent(ZipUtils.gzip(wmNews.getContent()));
        // 调用mapper层进行保存
        this.apArticleContentMapper.insert(apArticleContent);

        // 保存文章配置信息
        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        apArticleConfig.setIsComment(true);
        apArticleConfig.setIsDelete(false);
        apArticleConfig.setIsDown(false);
        apArticleConfig.setIsForward(true);
        // 调用mapper层进行保存
        this.apArticleConfigMapper.insert(apArticleConfig);

        // 创建ES索引
        EsIndexEntity entity = new EsIndexEntity();
        entity.setId(apArticle.getId().longValue());
        entity.setChannelId(new Long(apArticle.getChannelId()));
        entity.setContent(ZipUtils.gunzip(apArticleContent.getContent()));
        entity.setPublishTime(apArticle.getPublishTime());
        entity.setStatus(1L);
        entity.setTag("media");
        entity.setTitle(apArticle.getTitle());
        if (wmNews.getUserId() != null) {
            entity.setUserId(wmNews.getUserId());
        }
        // new一个索引构造器
        Index index = new Index.Builder(entity)
                .id(apArticle.getId().toString())
                .refresh(true)
                .index(ESIndexConstants.ARTICLE_INDEX)
                .type(ESIndexConstants.DEFAULT_DOC)
                .build();
        JestResult result = null;
        try {
            result = this.jestClient.execute(index);
        } catch (IOException e) {
            log.error("执行ES创建索引失败，message:{}", e.getMessage());
        }
        // 判断插入结果
        if (result != null && !result.isSucceeded()) {
            log.error("插入更新索引失败：message:{}", result.getErrorMessage());
        }
        // 修改wmNews的状态为9
        wmNews.setArticleId(apArticle.getId());
        updateWmNews(wmNews, WmMediaConstans.WM_NEWS_PUBLISH_STATUS, "审核成功");

        // 通知文章迁移微服务，将审核通过的文章同步到hbase中
        ArticleReviewSuccessDto dto = new ArticleReviewSuccessDto();
        dto.setArticleId(apArticle.getId());
        dto.setType(ArticleReviewSuccessDto.ArticleReviewSuccessType.WEMEDIA);
        this.kafkaSender.sendArticleReviewSuccessMessage(new ArticleReviewSuccessMessage(dto));

        // 发消息通知自媒体用户文章审核通过
        saveApUserMessage(wmNews, AppConstans.REVIEW_SUCCESS, "文章审核成功");
    }
}
