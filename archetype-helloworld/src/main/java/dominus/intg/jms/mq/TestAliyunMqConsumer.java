package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.*;
import com.aliyuncs.exceptions.ClientException;
import dominus.framework.junit.annotation.MessageQueueTest;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class TestAliyunMqConsumer extends TestAliyunMqZBaseTestCase {

    Consumer consumer;
    Producer producer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        if (messageQueueAnnotation != null && messageQueueAnnotation.produceTestMessage()) {
            this.createTestTopic(testTopicId);
            this.createProducerPublish(testTopicId, testProducerId);
            producer = this.createProducer(testProducerId);
            produceTestMessage(producer, testTopicId, messageQueueAnnotation.count());
        }
        if (messageQueueAnnotation != null && StringUtils.hasText(messageQueueAnnotation.queueName()))
            testTopicId = messageQueueAnnotation.queueName();
        if (messageQueueAnnotation != null && !StringUtils.hasText(messageQueueAnnotation.consumerGroupId()))
        this.createConsumerSubscription(testTopicId, testConsumerId);

    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        if (producer != null) producer.shutdown();
        if (consumer != null) consumer.shutdown();
        //TODO delete exception in public cloud
        deleteConsumerSubscription(testTopicId, testConsumerId);
        if (messageQueueAnnotation != null && messageQueueAnnotation.produceTestMessage()) {
            this.deleteTestTopic(testTopicId);
        }
        if (messageQueueAnnotation != null && !StringUtils.hasText(messageQueueAnnotation.consumerGroupId())) {
            this.deleteProducerPublish(testTopicId, testProducerId);
        }

    }

    @MessageQueueTest(produceTestMessage = true, count = 100)
    @Test
    public void testSimpleConsumer() throws ClientException, InterruptedException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(messageQueueAnnotation.count());

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


    @MessageQueueTest(produceTestMessage = false, count = 20000, queueName = "D-GUOXU-TEST-20K-0620")
    @Test
    public void testConcurrentConsumer() throws ClientException, InterruptedException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(messageQueueAnnotation.count());
        final ConcurrentMap<Long, AtomicLong> map = new ConcurrentHashMap<>();
        int concurrency = 1000;

        consumer = this.createDefaultConsumer(testTopicId, testConsumerId, concurrency, MAX_RECONSUME_TIMES);
        consumer.subscribe(testTopicId, "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                latch.countDown();
                map.putIfAbsent(Thread.currentThread().getId(), new AtomicLong(0));
                map.get(Thread.currentThread().getId()).incrementAndGet();
                out.printf("%s -> consumed message, [key]=%s,[value]=%s\n",
                        Thread.currentThread().getId(), message.getKey(), new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        assertEquals(true, latch.await(5, TimeUnit.MINUTES));
        assertEquals(concurrency, map.keySet().size());
        int total = 0;
        for (Long id : map.keySet()) {
            out.println(map.get(id));
            total += map.get(id).intValue();
        }
        assertEquals(messageQueueAnnotation.count(), total);
    }


    /**
     * EE: 16 partitions in public cloud
     * DemoMessageListener Receive: ORDERID_3 [0A91883700001F9000000B8F31078CE5]
     * DemoMessageListener Receive: ORDERID_19 [0A91883700001F9000000B8F310C2ABF]
     * EE: ONS broker failed, only get 5000 messages.
     */
    @MessageQueueTest(produceTestMessage = false, consumerGroupId = "CID-D-GUOXU-TEST-1K-05241", queueName = "D-GUOXU-TEST-1K-0524", count = 1000)
    @Test
    public void testConsumeMessage() throws InterruptedException, ClientException {
        final CountDownLatch latch = new CountDownLatch(messageQueueAnnotation.count());

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
                        message.getReconsumeTimes(), message.getKey(), new String(message.getBody()));
                latch.countDown();
                return Action.ReconsumeLater;
            }
        });
        consumer.start();
        assertEquals(true, latch.await(5, TimeUnit.MINUTES));
    }


}
