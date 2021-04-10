package com.heima.apis.user;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/24 1:14
 * @version: V1.0
 * @Description: 用户关注/取消关注对外暴露的接口
 */
@Api(value = "用户关注管理", description = "用户关注管理接口，实现用户关注或取消关注某个人", tags = "UserRelationApi")
public interface UserRelationControllerApi {

    /**
     * 用户关注或取消关注一个人
     *
     * @param dto 封装参数对象
     * @return
     */
    @ApiOperation("用户关注或取消关注一个人")
    @ApiImplicitParam(name = "dto", value = "用户关注/取消关注条件", paramType = "body", required = true, dataTypeClass = UserRelationDto.class)
    ResponseResult follow(UserRelationDto dto);
}
