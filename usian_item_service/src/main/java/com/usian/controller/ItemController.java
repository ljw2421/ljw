package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.ItemService;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("service/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping("selectItemInfo")
    public TbItem selectItemInfo(Long itemId){
        return itemService.getById(itemId);
    }

    @RequestMapping("selectTbItemAllByPage")
    PageResult selectTbItemAllByPage(@RequestParam(defaultValue = "1")Integer page,
                                     @RequestParam(defaultValue = "2")Integer rows){
        return itemService.selectTbItemAllByPage(page,rows);
    }

    @RequestMapping("insertTbItem")
    public Integer insertTbItem(@RequestBody TbItem tbItem, String desc,
                                String itemParams){
        return itemService.insertTbItem(tbItem,desc,itemParams);
    }

    @RequestMapping("preUpdateItem")
    public Map<String,Object> preUpdateItem(Long itemId){
        return itemService.preUpdateItem(itemId);
    }

    @RequestMapping("updateTbItem")
    public Integer updateTbItem(@RequestBody TbItem tbItem, String desc,
                                String itemParams){
        return itemService.updateTbItem(tbItem,desc,itemParams);
    }

    @RequestMapping("deleteItemById")
    public void deleteItemById(@RequestParam(defaultValue = "0") Long itemId){

        itemService.deleteItemById(itemId);
    }

}
