package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.*;
import com.aliyuncs.exceptions.ClientException;
import dominus.framework.junit.annotation.MessageQueueTest;
import dominus.intg.jms.mq.endpoint.DemoMessageListener;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TestAliyunMqConsumer extends TestAliyunMqZBaseTestCase {

    Consumer consumer;
    Producer producer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        if (testAnnotation != null && testAnnotation.produceTestMessage()) {
            this.createTestTopic(testTopicId);
            this.createProducerPublish(testTopicId, testProducerId);
            producer = this.createProducer(testProducerId);
            produceTestMessage(producer, testTopicId, testAnnotation.count());
        }
        if (testAnnotation != null && StringUtils.hasText(testAnnotation.queueName()))
            testTopicId = testAnnotation.queueName();
        this.createConsumerSubscription(testTopicId, testConsumerId);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        if (producer != null) producer.shutdown();
        if (consumer != null) consumer.shutdown();
        //TODO delete exception in public cloud
        deleteConsumerSubscription(testTopicId, testConsumerId);
        if (testAnnotation != null && testAnnotation.produceTestMessage()) {
            this.deleteTestTopic(testTopicId);
            this.deleteProducerPublish(testTopicId, testProducerId);
        }

    }

    @MessageQueueTest(produceTestMessage = true, count = 100)
    @Test
    public void testSimpleConsumer() throws ClientException, InterruptedException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(testAnnotation.count());

        consumer = this.createDefaultConsumer(testTopicId, testConsumerId, 2, MAX_RECONSUME_TIMES);
        consumer.subscribe(testTopicId, "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                latch.countDown();
                out.printf("consumed message, [key]=%s,[value]=%s\n", message.getKey(), new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        assertEquals(true, latch.await(5, TimeUnit.MINUTES));
    }


    /**
     * EE: 16 partitions in public cloud
     * DemoMessageListener Receive: ORDERID_3 [0A91883700001F9000000B8F31078CE5]
     * DemoMessageListener Receive: ORDERID_19 [0A91883700001F9000000B8F310C2ABF]
     * EE: ONS broker failed, only get 5000 messages.
     */
    @MessageQueueTest(produceTestMessage = false, queueName = "D-GUOXU-TEST-1K-0520", count = 1000)
    @Test
    public void testConsumeMessage() throws InterruptedException, ClientException {
        final CountDownLatch latch = new CountDownLatch(testAnnotation.count());

        consumer = this.createDefaultConsumer(testTopicId, testConsumerId, 1, MAX_RECONSUME_TIMES);
        consumer.subscribe(testTopicId, "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                latch.countDown();
                out.printf("consumed message, [key]=%s,[value]=%s\n", message.getKey(), new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        assertEquals(true, latch.await(5, TimeUnit.MINUTES));
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-ONE-0520")
    @Test
    public void testResumeLater() throws ClientException, InterruptedException {
        //re-consume later for 3 times
        final CountDownLatch latch = new CountDownLatch(3);

        //sleep to wait for topic and publish info updated to name server.
        consumer = this.createDefaultConsumer(testTopicId, testConsumerId, 1, MAX_RECONSUME_TIMES);
        consumer.subscribe(testTopicId, "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                printf(ANSI_RED, "ReconsumeLater, [ReconsumeTime]:%d, [key]=%s,[value]=%s\n",
                        message.getReconsumeTimes(),message.getKey(), new String(message.getBody()));
                latch.countDown();
                return Action.ReconsumeLater;
            }
        });
        consumer.start();
        assertEquals(true, latch.await(5, TimeUnit.MINUTES));
    }


}
