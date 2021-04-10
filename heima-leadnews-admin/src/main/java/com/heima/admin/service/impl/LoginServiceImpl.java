package com.heima.admin.service.impl;

import com.google.common.collect.Maps;
import com.heima.admin.service.ILoginService;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mappers.admin.AdUserMapper;
import com.heima.model.media.pojos.WmUser;
import com.heima.utils.jwt.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: HuangMuChen
 * @date: 2021/2/3 16:09
 * @version: V1.0
 * @Description: 后台管理用户登录业务层实现类
 */
@Service
@Slf4j
public class LoginServiceImpl implements ILoginService {
    @Autowired
    private AdUserMapper adUserMapper;

    /**
     * 后台管理用户登录鉴权
     *
     * @param user
     * @return
     */
    @Override
    public ResponseResult login(AdUser user) {
        // 记录日志
        log.info("开始用户登录校验:{}", user.getName());
        // 参数校验
        if (StringUtils.isEmpty(user.getName()) || StringUtils.isEmpty(user.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE, "用户名或密码不能为空");
        }
        // 调用mapper层查询用户
        AdUser adUser = this.adUserMapper.queryUserByName(user.getName());
        // 判断
        if (adUser != null) {
            if (user.getPassword().equals(adUser.getPassword())) {
                Map<String, Object> map = Maps.newHashMap();
                // 由于没有对密码进行加密，所以向前端返回登录结果时不应该暴露用户密码和盐
                adUser.setPassword("");
                adUser.setSalt("");
                map.put("token", AppJwtUtil.getToken(adUser));
                map.put("user", adUser);
                return ResponseResult.okResult(map);
            } else {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
        } else {
            // 记录日志
            log.info("校验失败，用户不存在:{}", user.getName());
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");
        }
    }
}
