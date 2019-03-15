package cn.itcast.demo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class TopicProducer {
	@Autowired
	private JmsTemplate jmsTemplate;
	
	//@Qualifier("topicTextDestination")		这种方式,变量可以取任何名
	@Autowired
	private Destination topicTextDestination;
	
	/**
	 * 发送文本消息
	 * @param text
	 */
	public void sendTextMessage(final String text){
		jmsTemplate.send(topicTextDestination, new MessageCreator() {			
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		});		
	}
}
