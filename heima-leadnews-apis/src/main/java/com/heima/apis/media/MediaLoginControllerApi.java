package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.pojos.WmUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/2/1 10:54
 * @version: V1.0
 * @Description: 自媒体用户登录对外暴露的接口
 */
@Api(value = "自媒体用户登录管理", description = "自媒体用户登录管理接口，提供自媒体用户登录鉴权", tags = "MediaLoginApi")
public interface MediaLoginControllerApi {

    @ApiOperation("自媒体用户登录鉴权")
    @ApiImplicitParam(name = "dto", value = "自媒体用户登录鉴权条件", paramType = "body", required = false, dataTypeClass = WmUser.class)
    ResponseResult login(WmUser user);
}
