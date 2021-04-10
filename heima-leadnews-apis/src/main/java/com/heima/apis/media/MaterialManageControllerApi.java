package com.heima.apis.media;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dtos.WmMaterialDto;
import com.heima.model.media.dtos.WmMaterialListDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: HuangMuChen
 * @date: 2021/2/1 15:04
 * @version: V1.0
 * @Description: 媒资管理对外暴露的接口
 */
@Api(value = "媒资管理", description = "媒资管理接口，提供素材增、删、查，文章发布、删除、修改等功能", tags = "MaterialManageApi")
public interface MaterialManageControllerApi {

    @ApiOperation("上传图片")
    @ApiImplicitParam(name = "file", value = "上传文件", required = true, paramType = "form", dataType = "file")
    ResponseResult uploadPicture(MultipartFile file);

    @ApiOperation("删除图片")
    @ApiImplicitParam(name = "dto", value = "图片删除条件", paramType = "body", required = true, dataTypeClass = WmMaterialDto.class)
    ResponseResult delPicture(WmMaterialDto dto);

    @ApiOperation("分页查询图片列表")
    @ApiImplicitParam(name = "dto", value = "分页查询图片列表条件", paramType = "body", required = true, dataTypeClass = WmMaterialListDto.class)
    ResponseResult listPicture(WmMaterialListDto dto);

    @ApiOperation("收藏图片")
    @ApiImplicitParam(name = "dto", value = "收藏图片条件", paramType = "body", required = true, dataTypeClass = WmMaterialDto.class)
    ResponseResult collectionMaterial(WmMaterialDto dto);

    @ApiOperation("取消图片收藏")
    @ApiImplicitParam(name = "dto", value = "取消图片收藏条件", paramType = "body", required = true, dataTypeClass = WmMaterialDto.class)
    ResponseResult cancleCollectionMaterial(WmMaterialDto dto);
}
