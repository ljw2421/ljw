package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.config.RedisClient;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ContentService {

    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;

    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;

    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;

    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;

    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;

    @Value("${PORTAL_AD_KEY}")
    private String portal_ad_redis_key;

    @Autowired
    private RedisClient redisClient;

//    @Autowired
//    private RedisClient redisClient;

    @Autowired
    private TbContentMapper tbContentMapper;

    public PageResult selectTbContentAllByCategoryId(Long categoryId) {
        PageHelper.startPage(1, 999999);
        TbContentExample example = new TbContentExample();
        TbContentExample.Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> list = tbContentMapper.selectByExampleWithBLOBs(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);

        return new PageResult(1, pageInfo.getTotal(), pageInfo.getList());
    }

    public Integer deleteContentByIds(Long id) {
        redisClient.hdel(portal_ad_redis_key,AD_CATEGORY_ID);
//        redisClient.hdel(portal_ad_redis_key,AD_CATEGORY_ID.toString());
        return tbContentMapper.deleteByPrimaryKey(id);
    }

    public Integer insertTbContent(TbContent tbContent) {
        Date date = new Date();
        tbContent.setCreated(date);
        tbContent.setUpdated(date);
        Integer num = tbContentMapper.insertSelective(tbContent);
//        redisClient.hdel(portal_ad_redis_key,AD_CATEGORY_ID.toString());
        return num;
    }

    public List<AdNode> selectFrontendContentByAD() {
        TbContentExample tbContentExample = new TbContentExample();
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(AD_CATEGORY_ID);
        List<AdNode> adNodeList = new ArrayList<AdNode>();
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);
        for (TbContent tbContent : tbContentList) {
            AdNode adNode = new AdNode();
            adNode.setSrc(tbContent.getPic());
            adNode.setSrcB(tbContent.getPic2());
            adNode.setHref(tbContent.getUrl());
            adNode.setHeight(AD_HEIGHT);
            adNode.setWidth(AD_WIDTH);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidthB(AD_WIDTHB);
            adNodeList.add(adNode);
        }
        redisClient.hset(portal_ad_redis_key,AD_CATEGORY_ID.toString(),adNodeList);
//        redisClient.hset(portal_ad_redis_key,AD_CATEGORY_ID.toString(),adNodeList);
        return adNodeList;
    }
}
