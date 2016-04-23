package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.Consumer;
import com.aliyuncs.exceptions.ClientException;
import dominus.intg.jms.mq.endpoint.DemoMessageListener;
import dominus.intg.jms.mq.endpoint.ResumeMessageListener;
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

    /**
     * EE: 16 partitions in public cloud
     * DemoMessageListener Receive: ORDERID_3 [0A91883700001F9000000B8F31078CE5]
     * DemoMessageListener Receive: ORDERID_19 [0A91883700001F9000000B8F310C2ABF]
     * EE: ONS broker failed, only get 5000 messages.
     */
    @Test
    public void test10KConsumer() throws InterruptedException, ClientException {
        currentTopic = TestAliyunMqAdmin.TEST_10K_QUEUE;

        this.createConsumerSubscription(currentTopic, testConsumerId);
        //sleep to wait for topic and publish info updated to name server.
        consumer = this.createDefaultConsumer(currentTopic, testConsumerId, 2, 16, new DemoMessageListener());
        consumer.start();
        while (true) {
            if (DemoMessageListener.count.longValue() == 10000)
                break;
            else {
                printf(ANSI_RED, "Consumed Message: %d\n", DemoMessageListener.count.longValue());
                Thread.sleep(10 * Second);
            }
        }
        assertEquals(10000, DemoMessageListener.count.longValue());
    }

    //MaxReconsumeTimes = "maxReconsumeTimes"
    @Test
    public void testResumeMessage() throws ClientException, InterruptedException {
        currentTopic = TestAliyunMqAdmin.TEST_ONE_MSG_QUEUE;

        this.createConsumerSubscription(currentTopic, testConsumerId);
        //sleep to wait for topic and publish info updated to name server.
        consumer = this.createDefaultConsumer(currentTopic, testConsumerId, 1, 3, new ResumeMessageListener());
        consumer.start();
        Thread.sleep(5 * Minute);
    }


}
