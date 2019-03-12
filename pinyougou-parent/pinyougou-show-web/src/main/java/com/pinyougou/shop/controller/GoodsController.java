package com.pinyougou.shop.controller;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		//对表中商家ID的属性进行添加(通过security认证得到的商家ID)
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		//修改操作可能涉及到安全问题,如某账号修改其他账号的商品,是要进行过滤操作的
		//简单的操作是用登录账号的ID限制对其他数据 增删改查 这里仅仅对修改进行限制
		//获取当前的登录账号的ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		//获取数据库的商品( 通过当前传入的Goods商品的Id)
		Goods goods2 = goodsService.findOne(goods.getGoods().getId());
		
		if (!goods2.getGoods().getSellerId().equals(sellerId) || !goods.getGoods().getSellerId().equals(sellerId)) {
			return new Result(false, "非法操作");
		}
		
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		//为了满足显现时带着商家ID,我们用商家登录用户的ID设置ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		
		return goodsService.findPage(goods, page, rows);		
	}
	
	/**
	 * 更新上下架状态
	 * @param ids
	 * @param status
	 */
	@RequestMapping("/updateMarketable")
	public Result updateMarketable(Long[] ids, String marketable){
		try {
			//在修改上架状态前先要判断是否审核
			for(Long id:ids){
				Goods goods = goodsService.findOne(id);
				String status = goods.getGoods().getAuditStatus();
				//status=0是未审核的商品
				if (status.equals("0") || status == null || status.equals("2")) {
					return new Result(false, "存在未审核的商品");
				}
			}
			goodsService.updateMarketable(ids, marketable);
			//把审核过的商品存入solr,*******************未完成
			//goodsService.findItemListByGoodsIdandStatus(ids, status)
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}
	
}
