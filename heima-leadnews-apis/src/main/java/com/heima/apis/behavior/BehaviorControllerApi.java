package com.heima.apis.behavior;

import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.ShowBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author: HuangMuChen
 * @date: 2021/1/16 21:44
 * @version: V1.0
 * @Description: 用户行为对外暴露的接口
 */
@Api(value = "用户行为管理", description = "用户行为管理接口，提供用户行为的保存", tags = "BehaviorApi")
public interface BehaviorControllerApi {

    @ApiOperation("保存用户查看文章行为")
    @ApiImplicitParam(name = "dto", value = "用户行为信息", paramType = "body", required = true, dataTypeClass = ShowBehaviorDto.class)
    ResponseResult saveShowBehavior(ShowBehaviorDto dto);

    @ApiOperation("保存用户点赞行为")
    @ApiImplicitParam(name = "dto", value = "用户点赞信息", paramType = "body", required = true, dataTypeClass = LikesBehaviorDto.class)
    ResponseResult saveLikesBehavior(LikesBehaviorDto dto);

    @ApiOperation("保存用户不喜欢行为")
    @ApiImplicitParam(name = "dto", value = "用户不喜欢信息", paramType = "body", required = true, dataTypeClass = UnLikesBehaviorDto.class)
    ResponseResult saveUnLikesBehavior(UnLikesBehaviorDto dto);

    @ApiOperation("保存用户阅读行为")
    @ApiImplicitParam(name = "dto", value = "用户阅读信息", paramType = "body", required = true, dataTypeClass = ReadBehaviorDto.class)
    ResponseResult saveReadBehavior(ReadBehaviorDto dto);
}
