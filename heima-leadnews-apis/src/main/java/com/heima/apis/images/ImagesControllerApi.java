package com.heima.apis.images;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import java.awt.image.BufferedImage;

/**
 * @author: HuangMuChen
 * @date: 2021/3/16 17:32
 * @version: V1.0
 * @Description: 图片缓存管理对外暴露的接口
 */
@Api(value = "图片缓存管理", description = "图片缓存管理接口，提供热点文章图片缓存功能", tags = "ImagesApi")
public interface ImagesControllerApi {

    @ApiOperation("获取图片信息")
    @ApiImplicitParam(name = "url", value = "图片地址", required = true, paramType = "path", dataType = "String")
    BufferedImage getImage(String url) throws Exception;
}
