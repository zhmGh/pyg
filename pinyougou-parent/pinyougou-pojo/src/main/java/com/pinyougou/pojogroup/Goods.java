package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

public class Goods implements Serializable {
	private TbGoods goods;	//SPU基本信息
	private TbGoodsDesc goodsDesc;	//SPU扩展信息
	private List<TbItem> itemList;	//SKU
	
	public TbGoods getGoods() {
		return goods;
	}
	public void setGoods(TbGoods goods) {
		this.goods = goods;
	}
	public TbGoodsDesc getGoodsDesc() {
		return goodsDesc;
	}
	public void setGoodsDesc(TbGoodsDesc goodsDesc) {
		this.goodsDesc = goodsDesc;
	}
	public List<TbItem> getTbItems() {
		return itemList;
	}
	public void setTbItems(List<TbItem> tbItems) {
		this.itemList = tbItems;
	}
		
}
