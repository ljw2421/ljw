package com.usian.service;

import com.usian.config.RedisClient;
import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemCategoryService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Value("${PROTAL_CATRESULT_KEY}")
    private String portal_catresult_redis_key;

    @Autowired
    private RedisClient redisClient;
//    @Autowired
//    private RedisClient redisClient;

    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(id);
        criteria.andStatusEqualTo(1);
        List<TbItemCat> list = this.tbItemCatMapper.selectByExample(example);
        return list;
    }

    public CatResult selectItemCategoryAll() {
        CatResult redisCatResult1 = (CatResult) redisClient.get(portal_catresult_redis_key);
        if (redisCatResult1 != null){
            System.out.println("这是Redis中的");
            return redisCatResult1;
        }

//        CatResult redisCatResult1 = (CatResult)redisClient.get(portal_catresult_redis_key);
//        if (redisCatResult1 != null){
//            System.out.println("这是redis中的");
//            return redisCatResult1;
//        }
        System.out.println("这是数据库中的");
        CatResult catResult = new CatResult();
        catResult.setData(getCatList(0L));
        redisClient.set(portal_catresult_redis_key,catResult);
//        redisClient.set(portal_catresult_redis_key,catResult);
        return catResult;
    }

    private List<?> getCatList(Long parentId){
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> list = tbItemCatMapper.selectByExample(example);
        List resultList = new ArrayList();
        int count = 0;
        for (TbItemCat tbItemCat : list) {
            if (tbItemCat.getIsParent()) {
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatList(tbItemCat.getId()));
                resultList.add(catNode);
                count++;
                if (count == 18){
                    break;
                }
            }
            resultList.add(tbItemCat.getName());
        }
        return resultList;
    }
}
