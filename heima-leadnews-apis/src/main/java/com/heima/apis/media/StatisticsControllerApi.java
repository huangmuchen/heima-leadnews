package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dtos.StatisticDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: HuangMuChen
 * @date: 2021/2/7 10:11
 * @version: V1.0
 * @Description: 图文统计对外暴露的接口
 */
@Api(value = "图文、粉丝统计接口", description = "图文、粉丝统计接口，提供文章统计信息的查询", tags = "StatisticsApi")
public interface StatisticsControllerApi {

    @ApiOperation("根据时间查询用户图文统计信息")
    @ApiImplicitParam(name = "dto", value = "根据时间查询用户图文统计信息条件", paramType = "body", required = true, dataTypeClass = StatisticDto.class)
    ResponseResult queryWmNewsStatisticsInfo(StatisticDto dto);

    @ApiOperation("根据时间查询用户粉丝概况统计信息")
    @ApiImplicitParam(name = "dto", value = "根据时间查询用户粉丝概况统计信息条件", paramType = "body", required = true, dataTypeClass = StatisticDto.class)
    ResponseResult queryWmFansStatisticsInfo(@RequestBody StatisticDto dto);
}
