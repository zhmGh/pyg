package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//1.创建参数
		Map<String,String> param = new HashMap<>();
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "品优购商品");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee", total_fee);//总金额（分）
		param.put("spbill_create_ip", "127.0.0.1");//IP
		param.put("notify_url", "http://test.itcast.cn");//回调地址(随便写)
		param.put("trade_type", "NATIVE");//交易类型
		
		//2.发送请求
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);//根据算法自动生成签名
			System.out.println(xmlParam);
			
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);//开启https请求(默认是false)
			client.setXmlParam(xmlParam);
			client.post();
			//3.获取结果
			String result = client.getContent();
			System.out.println(result);
			
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
			//创建一个map对象,用于返回我们自己需求的数据
			Map<String, String> map=new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));//支付地址
			map.put("total_fee", total_fee);//总金额
			map.put("out_trade_no",out_trade_no);//订单号
			return map;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	/**
	 * 通过订单号一直循环程序(监听)
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map param = new HashMap<>();
		param.put("appid", appid);//公众号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		
		try {
			//根据算法自动生成签名
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			//向微信服务查询支付状态
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);//开启https请求(默认是false)
			client.setXmlParam(xmlParam);
			client.post();
			//得到微信返回的结果
			String result = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
			System.out.println(resultMap);
			//正常情况下,需要的返回值是支付状态 成功 或 失败
			return resultMap;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//运行异常,造成没查到返回值(null)
			return null;
		}
	}
	
	@Override
	public Map closePay(String out_trade_no) {
		Map param=new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		String url="https://api.mch.weixin.qq.com/pay/closeorder";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	

}
