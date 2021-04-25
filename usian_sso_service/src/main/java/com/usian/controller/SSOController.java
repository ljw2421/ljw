package com.usian.controller;

import com.usian.Service.SSOService;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("service/sso")
public class SSOController {

    @Autowired
    private SSOService ssoService;

    @RequestMapping("checkUserInfo/{checkValue}/{checkFlag}")
    public Boolean checkUserInfo(@PathVariable String checkValue,@PathVariable Integer checkFlag){
        return ssoService.checkUserInfo(checkValue,checkFlag);
    }

    @RequestMapping("userRegister")
    public Integer userRegister(@RequestBody TbUser tbUser){
        return ssoService.userRegister(tbUser);
    }

    @RequestMapping("userLogin")
    public Map<String, Object> userLogin(@RequestBody TbUser tbUser){
        return ssoService.userLogin(tbUser);
    }

    @RequestMapping("getUserByToken/{token}")
    @ResponseBody
    public TbUser getUserByToken(@PathVariable String token){
        return ssoService.getUserByToken(token);
    }

    @RequestMapping("logOut")
    public Boolean logOut(@RequestBody String token){
        return ssoService.logOut(token);
    }
}
