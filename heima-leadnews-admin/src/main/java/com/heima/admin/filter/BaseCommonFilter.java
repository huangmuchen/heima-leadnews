package com.heima.admin.filter;

import com.heima.model.admin.dtos.CommonDto;
import com.heima.model.admin.dtos.CommonWhereDto;
import com.heima.model.admin.pojos.AdUser;

/**
 * @author: HuangMuChen
 * @date: 2021/2/9 10:17
 * @version: V1.0
 * @Description: 通用过滤器的过滤类：用于定义后置处理的接口约束和公用默认方法
 */
public interface BaseCommonFilter {

    /**
     * 查询后置通知
     *
     * @param user
     * @param dto
     */
    void doListAfter(AdUser user, CommonDto dto);

    /**
     * 更新后置通知
     *
     * @param user
     * @param dto
     */
    void doUpdateAfter(AdUser user, CommonDto dto);

    /**
     * 新增后置通知
     *
     * @param user
     * @param dto
     */
    void doInsertAfter(AdUser user, CommonDto dto);

    /**
     * 删除后置通知
     *
     * @param user
     * @param dto
     */
    void doDeleteAfter(AdUser user, CommonDto dto);

    /**
     * 获取更新字段里面的值
     *
     * @param field
     * @param dto
     * @return
     */
    default CommonWhereDto findUpdateValue(String field, CommonDto dto) {
        if (dto != null) {
            for (CommonWhereDto cw : dto.getSets()) {
                if (field.equals(cw.getFiled())) {
                    return cw;
                }
            }
        }
        return null;
    }

    /**
     * 获取查询字段里面的值
     *
     * @param field
     * @param dto
     * @return
     */
    default CommonWhereDto findWhereValue(String field, CommonDto dto) {
        if (dto != null) {
            for (CommonWhereDto cw : dto.getWhere()) {
                if (field.equals(cw.getFiled())) {
                    return cw;
                }
            }
        }
        return null;
    }
}
