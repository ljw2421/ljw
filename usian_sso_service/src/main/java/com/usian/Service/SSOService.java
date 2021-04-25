package com.usian.Service;

import com.usian.config.RedisClient;
import com.usian.mapper.TbUserMapper;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbUser;
import com.usian.pojo.TbUserExample;
import com.usian.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SSOService {

    @Autowired
    private TbUserMapper tbUserMapper;

    @Autowired
    private RedisClient redisClient;


    @Value("${USER_INFO}")
    private String USER_INFO;

    /**
     * 对用户的注册信息(用户名与电话号码)做数据校验
     */
    public Boolean checkUserInfo(String checkValue, Integer checkFlag) {
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        if (checkFlag == 1) {
            //用户名
            criteria.andUsernameEqualTo(checkValue);
        }else if (checkFlag == 2){
            //电话号码
            criteria.andPhoneEqualTo(checkValue);
        }else {
            //前台数据传输错误
            return false;
        }
        //从用户表中查询数据
        List<TbUser> tbUsers = tbUserMapper.selectByExample(example);
        if (tbUsers == null || tbUsers.size() == 0){
            //用户名和电话号码没有重复的可以通过
            return true;
        }
        return false;
    }
    /**
     * 用户的注册信息
     */
    public Integer userRegister(TbUser tbUser) {
        Date date = new Date();
        tbUser.setCreated(date);
        tbUser.setUpdated(date);
        tbUser.setPassword(MD5Utils.digest(tbUser.getPassword()));
        return tbUserMapper.insert(tbUser);
    }

    public Map<String, Object> userLogin(TbUser user) {
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(user.getUsername());
        criteria.andPasswordEqualTo(MD5Utils.digest(user.getPassword()));
        List<TbUser> tbUsers = tbUserMapper.selectByExample(example);
        if(tbUsers == null || tbUsers.size() == 0){
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        String token = UUID.randomUUID().toString();
        map.put("token", token);
        map.put("userid", tbUsers.get(0).getId());
        map.put("username", tbUsers.get(0).getUsername());
        String key = USER_INFO + ":" + token;
        // 将登录信息放入redis中
        redisClient.set(key, tbUsers.get(0));
        redisClient.expire(key, 1800);
        return map;
    }

    public TbUser getUserByToken(String token) {
        TbUser tbUser = (TbUser)redisClient.get(USER_INFO + ":" + token);
        if (tbUser != null){
            redisClient.expire(USER_INFO + ":" + token,1800);
            return tbUser;
        }
        return null;
    }

    public Boolean logOut(String token) {
        return redisClient.del(USER_INFO + ":" + token);
    }
}
