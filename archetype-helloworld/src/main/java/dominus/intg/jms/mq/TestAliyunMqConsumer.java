package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.Consumer;
import com.aliyuncs.exceptions.ClientException;
import dominus.intg.jms.mq.endpoint.DemoMessageListener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAliyunMqConsumer extends TestAliyunMqZBaseTestCase {

    Consumer consumer;
    String currentTopic;


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        if (consumer != null) consumer.shutdown();
        //TODO delete exception in public cloud
        deleteConsumerSubscription(currentTopic, testConsumerId);
    }

    @Test
    public void test10KConsumer() throws InterruptedException, ClientException {
        currentTopic = TestAliyunMqAdmin.TEST_10K_QUEUE;

        this.createConsumerSubscription(currentTopic, testConsumerId);
        //sleep to wait for topic and publish info updated to name server.
        consumer = this.createDefaultConsumer(currentTopic, testConsumerId);
        consumer.start();
        while (true) {
            if (DemoMessageListener.count.longValue() == 10000)
                break;
            else
                Thread.sleep(10 * Second);
        }
        assertEquals(10000, DemoMessageListener.count.longValue());
    }

    //MaxReconsumeTimes = "maxReconsumeTimes"
    @Test
    public void testResumeMessage() throws ClientException, InterruptedException {
        currentTopic = TestAliyunMqAdmin.TEST_ONE_MSG_QUEUE;

        this.createConsumerSubscription(currentTopic, testConsumerId);
        //sleep to wait for topic and publish info updated to name server.
        consumer = this.createDefaultConsumer(currentTopic, testConsumerId);
        consumer.start();
        Thread.sleep(5 * Minute);
    }


}
