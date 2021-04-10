package com.heima.admin.controller.v1;

import com.heima.admin.service.ILoginService;
import com.heima.admin.service.impl.LoginServiceImpl;
import com.heima.apis.admin.AdminLoginControllerApi;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: HuangMuChen
 * @date: 2021/2/3 16:05
 * @version: V1.0
 * @Description: 后台管理用户登录控制层
 */
@RestController
@RequestMapping("/login")
public class AdminLoginController implements AdminLoginControllerApi {
    @Autowired
    private ILoginService loginService;

    /**
     * 后台管理用户登录鉴权
     *
     * @param user
     * @return
     */
    @PostMapping("/in")
    @Override
    public ResponseResult login(@RequestBody AdUser user) {
        // 调用service层进行登录校验
        return this.loginService.login(user);
    }
}
