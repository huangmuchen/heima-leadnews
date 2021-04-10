package com.heima.admin.service;

import com.heima.model.admin.dtos.CommonDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author: HuangMuChen
 * @date: 2021/2/9 10:25
 * @version: V1.0
 * @Description: 通用业务层接口
 */
public interface ICommonService {

    /**
     * 加载通用的数据列表：无条件查询，无条件统计，有条件的查询，有条件的统计
     *
     * @param dto
     * @return
     */
    ResponseResult list(CommonDto dto);

    /**
     * 新增/修改通用的数据列表：通过dto中的model来判断，选择使用新增或修改
     *
     * @param dto
     * @return
     */
    ResponseResult update(CommonDto dto);

    /**
     * 删除通用的数据列表
     *
     * @param dto
     * @return
     */
    ResponseResult delete(CommonDto dto);
}
