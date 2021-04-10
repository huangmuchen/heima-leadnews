package com.heima.admin.service;

import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author: HuangMuChen
 * @date: 2021/2/3 16:09
 * @version: V1.0
 * @Description: 后台管理用户登录业务层接口
 */
public interface ILoginService {

    /**
     * 后台管理用户登录鉴权
     *
     * @param user
     * @return
     */
    ResponseResult login(AdUser user);
}
