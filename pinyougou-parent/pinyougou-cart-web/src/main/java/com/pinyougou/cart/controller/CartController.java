package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;


@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	/**
	 * 查找购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人:"+username);
		
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		//第一次获取时,购物车内容是null或"",定义该集合为"[]"
		if(cartListString == null || cartListString.equals("")) {
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		
		if (username.equals("anonymousUser")) {//未登录时的,默认认证名
			
			//System.out.println("从cookie获取数据");
			return cartList_cookie;
		}else {//已登录
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			System.out.println("从redis获取数据");
			if(cartList_cookie.size()>0){//如果本地存在购物车
				//合并购物车
				cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);	
				//清除本地cookie的数据
				util.CookieUtil.deleteCookie(request, response, "cartList");
				//将合并后的数据存入redis 
				cartService.saveCartListToRedis(username, cartList_redis); 
			}
			return cartList_redis;
		}
 	}
	
	
	/**
	 * 添加商品到购物车
	 * @param request
	 * @param response
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	//@CrossOrigin(origins="http://localhost:9105")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		//解决跨域请求的方式
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人:"+username);
		
		try {
			List<Cart> cartList =findCartList();//获取购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if (username.equals("anonymousUser")) {//未登录时的,默认认证名
				//存入cookie中
				util.CookieUtil.setCookie(request, response, "cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
				System.out.println("向cookie存入数据");
			}else {//已登录,存入redis中
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "购物车添加成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "购物车添加失败");
		}
	}
	
}
