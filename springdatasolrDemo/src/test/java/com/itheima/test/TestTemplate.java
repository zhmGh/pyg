package com.itheima.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.itheima.pojo.TbItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-solr.xml")
public class TestTemplate {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Test
	public void testAdd() {
		
		TbItem item = new TbItem();
		item.setId(1L);
		item.setTitle("小米9");
		item.setCategory("手机");
		item.setSeller("小米旗舰店");
		item.setGoodsId(1L);
		item.setPrice(new BigDecimal(2999.00));
		
		
		solrTemplate.saveBean(item);
		solrTemplate.commit();
	}
	
	@Test
	public void findById() {
		
		TbItem item = solrTemplate.getById(1L,TbItem.class);
		System.out.println(item.getId());
		
	}
	
	@Test
	public void deleteById() {
		
		solrTemplate.deleteById("1");
		solrTemplate.commit();
		
	}
	
	@Test
	public void testAddList() {
		
		ArrayList<TbItem> list = new ArrayList<TbItem>();
		for (int i = 0; i < 100; i++) {
			TbItem item = new TbItem();
			item.setId(i+1L);
			item.setBrand("小米");
			item.setTitle("小米X"+i);
			item.setCategory("手机");
			item.setSeller("小米旗舰店");
			item.setGoodsId(1L);
			item.setPrice(new BigDecimal(2999.00+i));
			
			list.add(item);
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	
	@Test
	public void testPageQuery() {
		
		Query query = new SimpleQuery("*:*");
		
		query.setOffset(20);
		query.setRows(20);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		List<TbItem> list = page.getContent();
		for(TbItem item:list) {
			System.out.println(item.getBrand()+"的手机,新款叫	"+item.getTitle()+",	价格是	"+item.getPrice());
		}
		
		long totalElements = page.getTotalElements();
		System.out.println("总记录数:"+totalElements);
		int totalPages = page.getTotalPages();
		System.out.println("总页数:"+totalPages);
	}
	
	@Test
	public void testPageQueryMutil() {
		
		Query query = new SimpleQuery("*:*");
		Criteria criteria= new Criteria("item_category").contains("手机");
		criteria.and("item_brand").contains("2");
		
		
		query.addCriteria(criteria);
		
		//query.setOffset(20);
		//query.setRows(20);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		List<TbItem> list = page.getContent();
		for(TbItem item:list) {
			System.out.println(item.getBrand()+"的手机,新款叫	"+item.getTitle()+",	价格是	"+item.getPrice());
		}
		
		long totalElements = page.getTotalElements();
		System.out.println("总记录数:"+totalElements);
		int totalPages = page.getTotalPages();
		System.out.println("总页数:"+totalPages);
	}
	
	@Test
	public void deleteAll() {
		Query query = new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}
	
}
