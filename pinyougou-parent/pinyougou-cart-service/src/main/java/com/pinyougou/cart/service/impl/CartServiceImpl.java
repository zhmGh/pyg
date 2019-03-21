package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;


/**
 * 购物车服务实现类
 * @author Administrator
 *
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 将商品添加到购物车里
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据商品SKU ID查询SKU商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item==null) {
			throw new RuntimeException("该商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("该商品未审核");
		}
		
		//2.获取商家ID
		String sellerId = item.getSellerId();
		
		//3.根据商家ID判断购物车列表中是否存在该商家的购物车
		Cart cart = searchCartBySellerId(cartList,sellerId);
		if(cart==null) {//4.如果购物车列表中不存在该商家的购物车
			//4.1 新建购物车对象 
			cart= new Cart();
			//4.2将购物车对象添加到购物车列表
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());//item表中有商家姓名属性
			
			List orderItemList = new ArrayList<>();
			//这里封装了一个创建orderItem类的方法
			TbOrderItem orderItem=createOrderItem(item,num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			cartList.add(cart);
			
		}else {//5.如果购物车列表中存在该商家的购物车
			// 判断购物车明细列表中是否存在该商品
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			
			if (orderItem==null) {//5.1. 如果没有，新增购物车明细
				orderItem = createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
				
			}else {//5.2. 如果有，在原购物车明细上添加数量，更改金额
				orderItem.setNum(orderItem.getNum()+num);//num可能是整数,也可能是负数
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
				
				if(orderItem.getNum()<=0) {//当前商品的数量是0或者0以下,移除该商品
					cart.getOrderItemList().remove(orderItem);//移除购物车明细
				}
				
				if (cart.getOrderItemList().size()==0) {//购物车内的商品列表内容是空
					cartList.remove(cart);//移除该商家的购物车列表
				}
			}
		}
		return cartList;
	}
	
	/**
	 * 根据商品ID判断购物明细列表中是否存在该商品
	 * @param orderItemList //购物车列表
	 * @param itemId	//购物车的商品ID
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
		for(TbOrderItem orderItem:orderItemList) {
			//判断是否是同一个商家
			if(orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		//当循环走完,还没有匹配返回,说明不存在同一商品
		return null;
	}
	
	/**
	 * 根据商家ID判断购物车列表中是否存在该商家的购物车
	 * @param cartList //购物车列表
	 * @param sellerId	//商家ID
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for(Cart cart:cartList) {
			//判断是否是同一个商家
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		//当循环走完,还没有匹配返回,说明不存在同一商家购物车
		return null;
	}
	
	/**
	 * 根据数量和商品创建订单(加入购物车一次只有一个SKU)
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if (num<=0) {
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setItemId(item.getId());//商品ID
		orderItem.setGoodsId(item.getGoodsId());//SPU的ID
		orderItem.setNum(num);//商品购买数量
		orderItem.setPicPath(item.getImage());//商品图片地址
		orderItem.setPrice(item.getPrice());//商品单价
		orderItem.setSellerId(item.getSellerId());//商家ID
		orderItem.setTitle(item.getTitle());//商品标题
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));//商品总金额
		return orderItem;
	}

	/**
	 * redis中提取购物车数据
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车数据....."+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		//System.out.println(cartList);
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		//System.out.println(cartList);
		return cartList;
	}

	/**
	 * 向redis存入购物车数据
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis存入购物车数据....."+username);
		redisTemplate.boundHashOps("cartList").put(username,cartList);
	}
	
	/**
	 * 合并购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		for(Cart cart: cartList2){
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());		
			}			
		}		
		return cartList1;
	}
	

}
