package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idworker;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加订单(不同商家的购物车添加不同的订单)
	 */
	@Override
	public void add(TbOrder order) {
		
		//1.从redis中提取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		
		List<String> orderList = new ArrayList<>();//订单ID列表
		double total_money=0;//总金额(方法内全局变量)
		
		//2.循环购物车列表添加订单
		for(Cart cart:cartList) {
			//此处应用到IdWorker,是为了解决当数据库分片时,ID易造成重复冲突(推特雪花算法)
			long orderId = idworker.nextId();
			//创建订单对象
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);//订单ID,由IdWorker生成
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setPostFee("0");//邮费
			tbOrder.setStatus("1");//默认未付款状态
			tbOrder.setCreateTime(new Date());//订单创建时间
			tbOrder.setUpdateTime(new Date());//订单更新时间
			//tbOrder.setPaymentTime(new Date());//支付时间
			//tbOrder.setConsignTime(new Date());//发货时间
			//tbOrder.setEndTime(new Date());//交易完成时间
			//tbOrder.setEndTime(new Date());//交易关闭时间
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地区名称(省，市，县)街道
			tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人电话
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
			tbOrder.setSellerId(cart.getSellerId());//商家ID
			
			//循环购物车明细
			double money=0;
			for(TbOrderItem orderItem:cart.getOrderItemList()) {
				orderItem.setId(idworker.nextId());
				orderItem.setOrderId(orderId);//
				orderItem.setSellerId(cart.getSellerId());
				orderItemMapper.insert(orderItem);
				
				money+=orderItem.getTotalFee().doubleValue();//累加商品金额
			}
			tbOrder.setPayment(new BigDecimal(money));//实付金额
			orderMapper.insert(tbOrder);
			
			//为了将订单存储到数据库中,先将每个商家的订单号放入集合
			orderList.add(orderId+"");//添加到订单列表
			total_money+=money;//累加到总金额 
		}
		
		//将TbPayLog存入数据库
		if ("1".equals(order.getPaymentType())) {//当支付方式是微信时
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idworker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());//创建日期
			payLog.setTotalFee((long)(total_money*100));//支付金额（分）
			payLog.setUserId(order.getUserId());//用户ID
			payLog.setTransactionId(order.getReceiverMobile());//交易号码
			payLog.setTradeState("0");//交易状态   0是未支付  1是已支付
			//setOrderList接收的是一个字符串对象,要将orderList集合对象转换为字符串
			String ids = orderList.toString().replace("[","").replace("]","");//去除符号"[","]"
			payLog.setOrderList(ids);//订单编号列表
			payLog.setPayType("1");//支付类型  1微信
			//存入数据库
			payLogMapper.insert(payLog);//插入到支付日志表
			//存入redis
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
			
		}
		
		
		//3.清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 从缓存中获取payLog对象
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog)redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//交易状态   0是未支付  1是已支付
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		String orderList = payLog.getOrderList();
		String[] orderIds = orderList.split(",");
		
		for(String orderId:orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (order!=null) {
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			}
		}
		
		//清除redis缓存数据	
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}
	
}
