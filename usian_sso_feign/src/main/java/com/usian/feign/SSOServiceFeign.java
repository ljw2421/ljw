package com.usian.feign;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient("usian-sso-service")
public interface SSOServiceFeign {

    @RequestMapping("/service/sso/checkUserInfo/{checkValue}/{checkFlag}")
    Boolean checkUserInfo(@PathVariable String checkValue, @PathVariable Integer checkFlag);

    @RequestMapping("/service/sso/userRegister")
    Integer userRegister(@RequestBody TbUser tbUser);

    @RequestMapping("/service/sso/userLogin")
    Map<String, Object> userLogin(@RequestBody TbUser tbUser);

    @RequestMapping("/service/sso/getUserByToken/{token}")
    TbUser getUserByToken(@PathVariable String token);

    @RequestMapping("/service/sso/logOut")
    Boolean logOut(@RequestBody String token);
}
