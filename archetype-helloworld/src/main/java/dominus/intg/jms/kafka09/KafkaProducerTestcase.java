package dominus.intg.jms.kafka09;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * EE: Consumer API, Producer API, Admin API
 * <p/>
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
        producer = this.createDefaultProducer(null);
    }

    @Override
    protected void doTearDown() throws Exception {
        producer.close();
        this.deleteTestTopic(testTopicName);
        super.doTearDown();
    }


    //TODO Throw exception or not? retry two times and throw RuntimeException
    //TODO Message timeout
    @Test
    public void testDefaultProducer() throws InterruptedException, ExecutionException, TimeoutException {
        long count = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));
        Random rnd = new Random();

        StopWatch watch = new StopWatch("[KafkaSimpleProducer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message = new ProducerRecord<String, String>(testTopicName, ip, info);

            RecordMetadata medadata = ((RecordMetadata) producer.send(message).get(10, TimeUnit.SECONDS));
            logger.info("[acknowledged message]:%s, %s, %s\n", medadata.topic(), medadata.partition(), medadata.offset());
        }
        watch.stop();
        System.out.println(watch);
        assertEquals(count, sumPartitionOffset(brokerList, testTopicName));
    }



}
