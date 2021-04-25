package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.config.RedisClient;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisClient redisClient;

    public TbItem getById(Long itemId) {
        //查询缓存中是否存在
        TbItem tbItem = (TbItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + BASE);
        //存在直接返回
        if (tbItem != null){
            return tbItem;
        }
        //不存在则从数据库中查询
        tbItem = itemMapper.selectByPrimaryKey(itemId);
        if (tbItem != null){
            //数据库中存在将数据保存至Redis中
            redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,tbItem);
            return tbItem;
        }
        //数据库中不存在则将空串保存到Redis中并且设置过期时间
        redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,null);
        redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,60);
        return tbItem;
    }

    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemExample tbItemExample = new TbItemExample();
        tbItemExample.setOrderByClause("updated Desc");
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> tbItemList = itemMapper.selectByExample(tbItemExample);
        for (int i = 0; i < tbItemList.size(); i++) {
            TbItem tbItem = tbItemList.get(i);
            tbItem.setPrice(tbItem.getPrice()/100);
        }
        PageInfo<TbItem> tbItemPageInfo = new PageInfo<>(tbItemList);
        PageResult pageResult = new PageResult();
        pageResult.setResult(tbItemPageInfo.getList());
        pageResult.setTotalPage(Long.valueOf(tbItemPageInfo.getPages()));
        pageResult.setPageIndex(tbItemPageInfo.getPageNum());
        return pageResult;
    }

    public Integer insertTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐 Tbitem 数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setCreated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        Integer i1 = itemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        Integer i2 = tbItemDescMapper.insertSelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        tbItemParamItem.setCreated(date);
        Integer i3 =tbItemParamItemMapper.insertSelective(tbItemParamItem);

        //添加商品发布消息到mq
        amqpTemplate.convertAndSend("item_exchage","item.add",itemId);

        return i1 + i2 + i3;
    }

    public Map<String, Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();
        //根据商品 ID 查询商品
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        map.put("item", item);
        //根据商品 ID 查询商品描述
        TbItemDesc itemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc", itemDesc.getItemDesc());
        //根据商品 ID 查询商品类目
        TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(item.getCid());
        map.put("itemCat", itemCat.getName());
        //根据商品 ID 查询商品规格信息
        TbItemParamItemExample example = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = example.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> list = tbItemParamItemMapper.selectByExampleWithBLOBs(example);
        if (list != null && list.size() > 0) {
            map.put("itemParamItem", list.get(0).getParamData());
        }
        return map;
    }

    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {

        Date date = new Date();
        tbItem.setCreated(date);
        tbItem.setUpdated(date);
        tbItem.setStatus((byte)1);
        Integer itemInteger = itemMapper.updateByPrimaryKey(tbItem);

        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setUpdated(date);
        tbItemDesc.setCreated(date);
        tbItemDesc.setItemDesc(desc);
        Integer tbItemDescInteger = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);

       /* TbItemParam tbItemParam = new TbItemParam();
        tbItemParam.setId(tbItem.getId());
        tbItemParam.setUpdated(date);
        tbItemParam.setCreated(date);
        tbItemParam.setParamData(itemParams);
        Integer tbItemParamInteger = tbItemParamMapper.updateByPrimaryKeySelective(tbItemParam);*/

        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());

        List<TbItemParamItem> list = tbItemParamItemMapper.selectByExample(tbItemParamItemExample);

        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(list.get(0).getItemId());
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        tbItemParamItem.setCreated(date);
        Integer tbItemParamInteger =tbItemParamItemMapper.insertSelective(tbItemParamItem);
        return itemInteger + tbItemDescInteger + tbItemParamInteger;
    }

    public void deleteItemById(Long itemId) {

        itemMapper.deleteItemById(itemId);
    }

    public TbItemDesc selectItemDescByItemId(Long itemId) {
        return tbItemDescMapper.selectByPrimaryKey(itemId);
    }

    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        TbItemParamItemExample example = new  TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = example.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> tbItemParamItems = tbItemParamItemMapper.selectByExampleWithBLOBs(example);
        if (tbItemParamItems != null && tbItemParamItems.size() >0){
            return tbItemParamItems.get(0);
        }
        return null;
    }
}
