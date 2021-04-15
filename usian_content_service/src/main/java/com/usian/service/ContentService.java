package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ContentService {

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
        return tbContentMapper.deleteByPrimaryKey(id);
    }

    public Integer insertTbContent(TbContent tbContent) {
        Date date = new Date();
        tbContent.setCreated(date);
        tbContent.setUpdated(date);
        return tbContentMapper.insertSelective(tbContent);
    }
}
