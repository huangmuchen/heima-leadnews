package com.heima.apis.article;

import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/22 10:32
 * @version: V1.0
 * @Description: 文章内容对外暴露的接口
 */
@Api(value = "文章内容管理", description = "文章内容管理接口，提供文章内容的加载", tags = "ArticleInfoApi")
public interface ArticleInfoControllerApi {

    @ApiOperation("加载文章详情")
    @ApiImplicitParam(name = "dto", value = "文章详情加载条件", paramType = "body", required = true, dataTypeClass = ArticleInfoDto.class)
    ResponseResult loadArticleInfo(ArticleInfoDto dto);

    @ApiOperation("加载文章详情的初始化配置信息")
    @ApiImplicitParam(name = "dto", value = "文章详情初始化配置加载条件", paramType = "body", required = true, dataTypeClass = ArticleInfoDto.class)
    ResponseResult loadArticleBehavior(ArticleInfoDto dto);
}
