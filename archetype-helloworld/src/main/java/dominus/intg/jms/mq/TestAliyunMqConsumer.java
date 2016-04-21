package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * From Aliyun User manual
 */
@ContextConfiguration(locations = {"classpath:spring-container/aliyun_mq_consumer_context.xml"})
public class TestAliyunMqConsumer extends DominusJUnit4TestBase {


    @Autowired
    Consumer consumer;

    String testTopic;

    @Override
    protected void doSetUp() throws Exception {
        testTopic = properties.getProperty("aliyun.mq.testTopic");
        out.printf("[Aliyun MQ] Test Topic:%s\n", testTopic);
        out.printf("[Producer] %s\n", FieldUtils.readDeclaredField(consumer, "consumer", true));
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();

    }

    @Test
    public void testNull() throws InterruptedException {

        Thread.sleep(5 * Minute);
    }


    public static class DemoMessageListener implements MessageListener {
        public Action consume(Message message, ConsumeContext context) {
            System.out.println("Receive: " + message.getMsgID());
            try {
                //do something..
                return Action.CommitMessage;
            } catch (Exception e) {
                //消费失败
                return Action.ReconsumeLater;
            }
        }
    }


}
