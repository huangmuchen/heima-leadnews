package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dtos.WmNewsDto;
import com.heima.model.media.dtos.WmNewsPageReqDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: HuangMuChen
 * @date: 2021/2/2 20:04
 * @version: V1.0
 * @Description: 文章管理对外暴露的接口
 */
@Api(value = "文章管理接口", description = "文章管理接口，提供文章的发布、查询、删除、修改", tags = "NewsApi")
public interface NewsControllerApi {

    @ApiOperation("分页查询文章列表")
    @ApiImplicitParam(name = "dto", value = "分页查询文章列表条件", paramType = "body", required = true, dataTypeClass = WmNewsPageReqDto.class)
    ResponseResult listNewsByUserId(WmNewsPageReqDto dto);

    @ApiOperation("根据id获取文章信息")
    @ApiImplicitParam(name = "dto", value = "根据id获取文章信息条件", paramType = "body", required = true, dataTypeClass = WmNewsDto.class)
    ResponseResult wmNews(WmNewsDto dto);

    @ApiOperation("根据id删除文章")
    @ApiImplicitParam(name = "dto", value = "根据id删除文章条件", paramType = "body", required = true, dataTypeClass = WmNewsDto.class)
    ResponseResult delNews(@RequestBody WmNewsDto dto);

    @ApiOperation("提交文章/保存草稿")
    @ApiImplicitParam(name = "dto", value = "提交文章/保存草稿条件", paramType = "body", required = true, dataTypeClass = WmNewsDto.class)
    ResponseResult summitNews(WmNewsDto dto);
}
