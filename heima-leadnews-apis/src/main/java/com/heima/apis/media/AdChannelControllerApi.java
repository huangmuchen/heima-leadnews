package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/2/1 13:54
 * @version: V1.0
 * @Description: 文章频道对外暴露的接口
 */
@Api(value = "文章频道管理", description = "文章频道接口，提供文章频道查询", tags = "AdChannelApi")
public interface AdChannelControllerApi {

    @ApiOperation("查询所有的频道")
    ResponseResult queryAllChannels();
}
