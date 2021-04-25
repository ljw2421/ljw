package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("frontend/cart")
public class CartController {

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @Value("${CART_COOKIE_EXPIRE}")
    private Integer CART_COOKIE_EXPIRE;

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    //将商品添加到购物车中
    @RequestMapping("addItem")
    public Result addItem(Long itemId, String userId, @RequestParam(defaultValue = "1") Integer num,
                          HttpServletRequest request,HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)) {
                /***********在用户未登录的状态下**********/
                // 1、从cookie中查询商品列表。
                Map<String, TbItem> cart = getCartFromCookie(request);

                //2、添加商品到购物车
                addItemToCart(cart, itemId, num);

                //4、把购车商品列表写入cookie
                addClientCookie(request, response, cart);
            } else {
                // 在用户已登录的状态
            }
            return Result.ok();
        } catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }
    /**
     * 把购车商品列表写入cookie
     * @param request
     * @param response
     * @param cart
     */
    private void addClientCookie(HttpServletRequest request,HttpServletResponse response,
                                 Map<String,TbItem> cart){
        String cartJson = JsonUtils.objectToJson(cart);
        CookieUtils.setCookie(request, response, this.CART_COOKIE_KEY, cartJson,
                CART_COOKIE_EXPIRE,true);
    }

    /**
     * 将商品添加到购物车中
     * @param cart
     * @param itemId
     * @param num
     */
    private void addItemToCart(Map<String, TbItem> cart, Long itemId,Integer num) {
        //从购物车中取商品
        TbItem tbItem = cart.get(itemId.toString());

        if(tbItem != null){
            //商品列表中存在该商品，商品数量相加。
            tbItem.setNum(tbItem.getNum() + num);
        }else{
            //商品列表中不存在该商品，根据商品id查询商品信息并添加到购车列表
            tbItem = itemServiceFeign.selectItemInfo(itemId);
            tbItem.setNum(num);
        }
        cart.put(itemId.toString(),tbItem);
    }

    /**
     * 获取购物车
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cartJson = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
        if (StringUtils.isNotBlank(cartJson)) {
            //购物车已存在
            Map<String, TbItem> map = JsonUtils.jsonToMap(cartJson, TbItem.class);
            return map;
        }
        //购物车不存在
        return new HashMap<String, TbItem>();
    }

    /**
     * 查看购物车
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId,HttpServletRequest
            request,HttpServletResponse response){
        try{
            List<TbItem> list = new ArrayList<TbItem>();
            if(StringUtils.isBlank(userId)){
                //在用户未登录的状态下
                Map<String,TbItem> cart = this.getCartFromCookie(request);
                Set<String> keys = cart.keySet();
                for(String key :keys){
                    list.add(cart.get(key));
                }
            }else{
                // 在用户已登录的状态
            }
            return Result.ok(list);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Result.error("error");
    }
}

