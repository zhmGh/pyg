package cn.itcast.sms;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;


/**
 * 消息监听类
 * @author Administrator
 */
@Component
public class SmsListener {
	
	@Autowired
	private SmsUtil smsUtil;
	
	//该方法监听的是sms ,故消息生产者也是sms
	@JmsListener(destination="sms")
	public void sendSms(Map<String,String> map) {
		try {
			SendSmsResponse response = smsUtil.sendSms(map.get("mobile"),map.get("sign_name"), map.get("template_code"),map.get("param"));
			System.out.println("Code=" + response.getCode());
	        System.out.println("Message=" + response.getMessage());
	        System.out.println("RequestId=" + response.getRequestId());
	        System.out.println("BizId=" + response.getBizId());	
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	
	
}
