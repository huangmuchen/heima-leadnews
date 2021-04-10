package com.heima.apis.article;

import com.heima.model.article.dtos.UserSearchDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/30 21:30
 * @version: V1.0
 * @Description: 文章搜索对外暴露的接口
 */
@Api(value = "文章搜索管理", description = "文章搜索管理接口，提供文章搜索相关功能", tags = "ArticleInfoApi")
public interface ArticleSearchControllerApi {

    @ApiOperation("查询用户搜索历史记录")
    @ApiImplicitParam(name = "dto", value = "查询用户搜索历史记录条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult loadSearchHistory(UserSearchDto dto);

    @ApiOperation("删除用户搜索历史记录")
    @ApiImplicitParam(name = "dto", value = "删除用户搜索历史记录条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult delSearchHistory(UserSearchDto dto);

    @ApiOperation("清空用户搜索历史记录")
    @ApiImplicitParam(name = "dto", value = "清空用户搜索历史记录条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult clearSearchHistory(UserSearchDto dto);

    @ApiOperation("查询今日热词")
    @ApiImplicitParam(name = "dto", value = "查询今日热词条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult loadHotKeyWords(UserSearchDto dto);

    @ApiOperation("模糊查询联想词")
    @ApiImplicitParam(name = "dto", value = "模糊查询联想词条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult searchAssociateWords(UserSearchDto dto);

    @ApiOperation("ES文章分页搜索")
    @ApiImplicitParam(name = "dto", value = "ES文章分页搜索条件", paramType = "body", required = true, dataTypeClass = UserSearchDto.class)
    ResponseResult esArticleSearch(UserSearchDto dto);
}
