package dominus.intg.jms.kafka;

import dominus.framework.junit.annotation.MessageQueueTest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * EE: Consumer API, Producer API, Admin API
 * <p>
 * [External Dependencies]
 * CDH Cluster(Kafka Cluster, Zookeeper);
 * Kafka Test Topic;
 */
public class KafkaProducerTestcase extends KafkaZBaseTestCase {


    Producer producer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        this.createTestTopic(testTopicName);
    }

    @Override
    protected void doTearDown() throws Exception {
        if (producer != null) producer.close();
        this.deleteTestTopic(testTopicName);
        super.doTearDown();
    }


    //TODO Throw exception or not? retry two times and throw RuntimeException
    //TODO Message timeout
    @MessageQueueTest(produceTestMessage = true, count = 10000)
    @Test
    public void testDefaultProducer() throws InterruptedException, ExecutionException, TimeoutException {
        producer = this.createDefaultProducer(null);
        long count = messageQueueAnnotation.count();
        Random rnd = new Random();

        StopWatch watch = new StopWatch("[KafkaSimpleProducer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message = new ProducerRecord<String, String>(testTopicName, ip, info);

            RecordMetadata medadata = ((RecordMetadata) producer.send(message).get(10, TimeUnit.SECONDS));
            logger.info("[acknowledged message]:{}, {}, {}", medadata.topic(), medadata.partition(), medadata.offset());
        }
        watch.stop();
        System.out.println(watch);
        assertEquals(count, sumPartitionOffset(brokerList, testTopicName));
    }

    @MessageQueueTest(produceTestMessage = true, count = 10000)
    @Test
    public void testBatchSend() throws InterruptedException, ExecutionException, TimeoutException {
        Properties prop = new Properties();
        prop.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-" + testTopicName);
        prop.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "204800");
        //EE "Batch Expired" exception when queueing records at a faster rate than they can be sent.
        prop.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");
        producer = this.createDefaultProducer(prop);
        int count = messageQueueAnnotation.count();
        Random rnd = new Random();
        CountDownLatch latch = new CountDownLatch(count);

        StopWatch watch = new StopWatch("[KafkaSimpleProducer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message = new ProducerRecord<String, String>(testTopicName, ip, info);

            producer.send(message, (medadata, exception) -> {
                if (exception != null) {
                    logger.error(exception.toString());
                } else {
                    logger.info("[acknowledged message]:{}, {}, {}", medadata.topic(), medadata.partition(), medadata.offset());
                }
                latch.countDown();
            });
        }
        watch.stop();
        System.out.println(watch);
        assertTrue(latch.await(120, TimeUnit.SECONDS));
        assertEquals(count, sumPartitionOffset(brokerList, testTopicName));
    }


    @MessageQueueTest(produceTestMessage = true, count = 2)
    @Test
    public void testBatchSendLinger() throws InterruptedException, ExecutionException, TimeoutException {
        Properties prop = new Properties();
        prop.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-" + testTopicName);
        prop.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "500");
        prop.setProperty(ProducerConfig.LINGER_MS_CONFIG, "6000");
        //EE: linger.ms
        // By default a buffer is available to send immediately even if there is additional unused space in the buffer. However if you want to reduce the number of requests you can set linger.ms to something greater than 0.
        //This will instruct the producer to wait up to that number of milliseconds before sending a request in hope that more records will arrive to fill up the same batch.
        producer = this.createDefaultProducer(prop);
        int count = messageQueueAnnotation.count();
        Random rnd = new Random();
        CountDownLatch latch = new CountDownLatch(count);

        StopWatch watch = new StopWatch("[KafkaSimpleProducer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message = new ProducerRecord<String, String>(testTopicName, ip, info);

            producer.send(message, (medadata, exception) -> {
                if (exception != null) {
                    logger.error(exception.toString());
                } else {
                    logger.info("[acknowledged message]:{}, {}, {}", medadata.topic(), medadata.partition(), medadata.offset());
                }
            });
            latch.countDown();
        }
        assertTrue(latch.await(120, TimeUnit.SECONDS));
        assertEquals(0, sumPartitionOffset(brokerList, testTopicName));
        producer.flush();
        assertEquals(2, sumPartitionOffset(brokerList, testTopicName));
    }

    @Test
    public void testPartitionsFor() throws InterruptedException {
        List<PartitionInfo> partitionInfos = _producer.partitionsFor(testTopicName);
        out.println(Arrays.toString(partitionInfos.toArray()));
        assertEquals(numPartitions, partitionInfos.size());
    }

}
