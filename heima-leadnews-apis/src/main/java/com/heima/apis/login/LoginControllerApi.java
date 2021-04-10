package com.heima.apis.login;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/29 20:09
 * @version: V1.0
 * @Description: App用户登录对外暴露的接口
 */
@Api(value = "App用户登录管理", description = "App用户登录管理接口，提供App用户登录鉴权", tags = "LoginApi")
public interface LoginControllerApi {

    @ApiOperation("App用户登录鉴权")
    @ApiImplicitParam(name = "dto", value = "App用户登录鉴权条件", paramType = "body", required = false, dataTypeClass = ApUser.class)
    ResponseResult login(ApUser user);
}
