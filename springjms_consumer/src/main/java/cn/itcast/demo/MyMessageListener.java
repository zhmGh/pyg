package cn.itcast.demo;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MyMessageListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		try {
			String text = textMessage.getText();
			System.out.println("接收到消息："+ text);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
