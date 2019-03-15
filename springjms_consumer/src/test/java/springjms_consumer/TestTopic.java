package springjms_consumer;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-jms-consumer-topic.xml")
public class TestTopic {
	@Test
	public void testTopic(){
		try {
			//启动监听,这种方式在正式结合spring时不会使用
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}	
}
