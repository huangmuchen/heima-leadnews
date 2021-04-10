package com.heima.apis.admin;

import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/2/3 15:59
 * @version: V1.0
 * @Description: 后台管理对外暴露的接口
 */
@Api(value = "后台管理系统", description = "后台管理系统接口，提供后台管理系统用户登录鉴权", tags = "AdminLoginApi")
public interface AdminLoginControllerApi {

    @ApiOperation("后台管理系统用户登录鉴权")
    @ApiImplicitParam(name = "dto", value = "后台管理系统用户登录鉴权条件", paramType = "body", required = false, dataTypeClass = AdUser.class)
    ResponseResult login(AdUser user);
}
