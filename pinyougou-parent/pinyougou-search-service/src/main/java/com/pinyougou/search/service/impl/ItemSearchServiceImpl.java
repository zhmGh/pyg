package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=3000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	/**
	 * 主要的查询方法
	 */
	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String,Object> map=new HashMap<>();
		
		//以前普通的查询方法实现
		/*Query query= new SimpleQuery();
		//添加查询条件 关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		//开启条件
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		map.put("rows", page.getContent());*/
		
		//1.调用高亮查询
		map.putAll(searchList(searchMap));
		
		//2.分组查询分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		
		//3.查询品牌和规格列表
		
		//优化操作
		//获取页面传来的category信息,判断是否为空
		String categoryName =(String) searchMap.get("category");
		if (!"".equals(categoryName)) {
			map.putAll(searchBrandAndSpecList(categoryName));
		}else {
			if(categoryList.size()>0) {
				//在查询的时候调用查询方法
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		return map;
		
	}
	
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		
		//创建查询对象
		Query query = new SimpleQuery("*:*");
		//关键字查询  相当于设置条件where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//设置分组选项  相当于group by
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组结果对象		参数一定是分组选项中出现过得域
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:entryList) {
			//获取分组的结果
			String groupValue = entry.getGroupValue();
			list.add(groupValue);
		}
		
		return list;
		
	}
	
	
	
	/**
	 * 根据关键字搜索列表
	 * @param keywords
	 * @return
	 */
	private Map searchList(Map searchMap){
		Map map=new HashMap();
		
		//*******************高亮初始化**********************
		//创建高亮对象
		HighlightQuery query = new SimpleHighlightQuery();
		//创建高亮选项,并设置作用域
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//高亮前后缀设置
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		//开启高亮选项设置
		query.setHighlightOptions(highlightOptions);
		
		//*******************过滤条件筛选**********************
		//1.1添加查询条件 关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		//开启条件设置
		query.addCriteria(criteria);
		
		//1.2按商品分类筛选
		if(!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filter = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filter);
		}
		
		//1.3按品牌分类筛选
		if(!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filter = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filter);
		}
		
		//1.4按规格分类筛选
		if(searchMap.get("spec")!=null) {
			//获取规格选项集合
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			//spec集合的key集合,即规格选项内容集合
			Set<String> keySet = specMap.keySet();
			for(String key:keySet) {
				//从规格选项内容集合中筛选
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filter = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filter);
			}
		}
		
		
		
		//***********************高亮**************************
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合(每条记录的高亮入口)所有高亮显示的内容
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for(HighlightEntry<TbItem> entry:entryList) {
			//获取高亮列表(所有高亮显示的域)
			List<Highlight> highlights = entry.getHighlights();
			
			if(highlights.size()>0 && highlights.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				//拿到
				item.setTitle(highlights.get(0).getSnipplets().get(0));
			}
		}
		
		map.put("rows",page.getContent());
		
		return map;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 根据商品分类名称查新品牌和规格列表
	 * @param category	商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap<>();
		//1.根据商品分类名称得到模板ID
		Long templateId =(Long)redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId!=null) {
			//2.根据模板ID获取品牌列表
			//System.out.println(templateId);
			List brandList = (List)redisTemplate.boundHashOps("brandList").get(templateId);
			//System.out.println(brandList);//null
			//存入map
			map.put("brandList", brandList);
			
			
			//3.根据模板ID获取规格列表
			List specList = (List)redisTemplate.boundHashOps("specList").get(templateId);
			//存入map
			map.put("specList", specList);
		}
		return map;
	}

}
