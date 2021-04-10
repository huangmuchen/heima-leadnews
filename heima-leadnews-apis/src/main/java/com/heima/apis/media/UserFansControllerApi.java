package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserFansPageReqDto;
import com.heima.model.user.dtos.UserPhotoDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: HuangMuChen
 * @date: 2021/2/7 13:41
 * @version: V1.0
 * @Description: 用户粉丝管理对外暴露的接口
 */
@Api(value = "用户粉丝管理接口", description = "用户粉丝管理接口，提供粉丝画像、列表查询", tags = "UserFansApi")
public interface UserFansControllerApi {

    @ApiOperation("查询用户粉丝画像")
    ResponseResult queryFansPortrait();

    @ApiOperation("分页查询用户粉丝列表")
    @ApiImplicitParam(name = "dto", value = "分页查询用户粉丝列表条件", paramType = "body", required = true, dataTypeClass = UserFansPageReqDto.class)
    ResponseResult queryFansList(UserFansPageReqDto dto);

    @ApiOperation("根据用户id查询用户头像")
    @ApiImplicitParam(name = "dto", value = "根据用户id查询用户头像条件", paramType = "body", required = true, dataTypeClass = UserPhotoDto.class)
    ResponseResult queryUserPhotoById(@RequestBody UserPhotoDto dto);
}
