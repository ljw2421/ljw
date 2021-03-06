package com.usian.controller;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbUser;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("frontend/sso")
public class SSOController {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    @RequestMapping("checkUserInfo/{checkValue}/{checkFlag}")
    public Result checkUserInfo(@PathVariable String checkValue,@PathVariable Integer checkFlag){
        Boolean checkUserInfo = ssoServiceFeign.checkUserInfo(checkValue,checkFlag);
        if (checkUserInfo){
            return Result.ok(checkUserInfo);
        }
        return Result.error("校验失败");
    }

    @RequestMapping("userRegister")
    public Result userRegister(TbUser tbUser){
        Integer i = ssoServiceFeign.userRegister(tbUser);
        if (i == 1){
            return Result.ok("注册成功");
        }
        return Result.error("注册失败");
    }

    @RequestMapping("userLogin")
    public Result userLogin(TbUser tbUser){
        Map<String,Object> map  = ssoServiceFeign.userLogin(tbUser);
        if (map != null){
            return Result.ok(map);
        }
        return Result.error("登录失败");
    }

    @RequestMapping("getUserByToken/{token}")
    @ResponseBody
    public Result getUserByToken(@PathVariable String token){
        TbUser tbUser  = ssoServiceFeign.getUserByToken(token);
        if (tbUser != null){
            return Result.ok(tbUser);
        }
        return Result.error("登录过期");
    }

    @RequestMapping("logOut")
    public Result logOut(String token){
        Boolean logOut  = ssoServiceFeign.logOut(token);
        if (logOut){
            return Result.ok();
        }
        return Result.error("退出失败");
    }

}
