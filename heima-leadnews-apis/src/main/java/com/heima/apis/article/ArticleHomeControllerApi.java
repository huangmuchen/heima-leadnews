package com.heima.apis.article;

import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/14 15:54
 * @version: V1.0
 * @Description: 文章首页对外暴露的接口
 */
@Api(value = "文章首页管理", description = "文章首页管理接口，提供文章首页数据的加载", tags = "ArticleHomeApi")
public interface ArticleHomeControllerApi {

    @ApiOperation("加载首页文章")
    @ApiImplicitParam(name = "dto", value = "文章首页加载条件", paramType = "body", required = false, dataTypeClass = ArticleHomeDto.class)
    ResponseResult load(ArticleHomeDto dto);

    @ApiOperation("加载更多数据")
    @ApiImplicitParam(name = "dto", value = "文章首页加载条件", paramType = "body", required = false, dataTypeClass = ArticleHomeDto.class)
    ResponseResult loadMore(ArticleHomeDto dto);


    @ApiOperation("加载最新数据")
    @ApiImplicitParam(name = "dto", value = "文章首页加载条件", paramType = "body", required = false, dataTypeClass = ArticleHomeDto.class)
    ResponseResult loadNew(ArticleHomeDto dto);
}
