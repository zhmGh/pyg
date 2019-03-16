package com.itheima.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {
	
	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;
	
	@RequestMapping("/send")
	public void send(String text){
		jmsMessagingTemplate.convertAndSend("itcast", text);
	}
	
	@RequestMapping("/sendmap")
	public void sendMap(){
		Map map=new HashMap<>();
		map.put("mobile", "13900001111");
		map.put("content", "恭喜获得10元代金券");		
		jmsMessagingTemplate.convertAndSend("itcast_map",map);
	}
	
	@RequestMapping("/sendsms")
	public void sendSms(){
		Map map=new HashMap<>();
		map.put("mobile", "13634148467");
		map.put("template_code", "SMS_134328095");	
		map.put("sign_name", "上进青年");
		map.put("param", "{\"code\":\"190315\"}");
		jmsMessagingTemplate.convertAndSend("sms",map);
	}
	
	
}
