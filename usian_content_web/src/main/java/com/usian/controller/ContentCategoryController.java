package com.usian.controller;

import com.usian.feign.ContentServiceFeign;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("content")
public class ContentCategoryController {

    @Autowired
    private ContentServiceFeign contentServiceFeign;

    @RequestMapping("selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0") Long id){
        List<TbContentCategory> list =  contentServiceFeign.selectContentCategoryByParentId(id);
        if(list != null && list.size() > 0){
            return Result.ok(list);
        }
        return Result.error("查询失败");
    }

    @RequestMapping("insertContentCategory")
    public Result insertContentCategory(TbContentCategory tbContentCategory){
        Integer i = contentServiceFeign.insertContentCategory(tbContentCategory);
        if(i == 1){
            return Result.ok();
        }
        return Result.error("添加分类失败");
    }

    @RequestMapping("updateContentCategory")
    public Result updateContentCategory(Long id,String name){
        Integer i = contentServiceFeign.updateContentCategory(id,name);
        if(i == 1){
            return Result.ok();
        }
        return Result.error("修改分类失败");
    }

    @RequestMapping("deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        Integer i = contentServiceFeign.deleteContentCategoryById(categoryId);
        if(i == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

}
