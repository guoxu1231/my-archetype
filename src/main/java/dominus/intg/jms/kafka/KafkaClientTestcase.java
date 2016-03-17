package dominus.intg.jms.kafka;


import dominus.intg.jms.kafka.consumer.KafkaConsumerConnector;
import dominus.intg.jms.kafka.producer.KafkaFastProducer;
import dominus.intg.jms.kafka.producer.KafkaReliableProducer;
import org.junit.Test;

import static org.junit.Assert.*;

import org.springframework.test.annotation.Repeat;

/**
 * EE: Consumer API, Producer API, Admin API
 * <p/>
 * [External Dependencies]
 * CDH Cluster(Kafka Cluster, Zookeeper);
 * Kafka Test Topic;
 */
public class KafkaClientTestcase extends KafkaZBaseTestCase {

    @Test
    @Repeat(1)
    public void testRoundRobinPartitioner() {
        long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));
        KafkaFastProducer.main(testTopicName, String.valueOf(events), brokerList);
        //total partition offset should be equal with events count.
        assertEquals(events, sumPartitionOffset());
    }

    @Test
    @Repeat(2)
    public void testKafkaClient() throws InterruptedException {

        final long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));

        KafkaConsumerConnector.main(testTopicName, groupId);
        //junit.framework.AssertionFailedError:Expected :500 Actual :244
        Thread.sleep(5000);
        new Thread() {
            @Override
            public void run() {
                KafkaFastProducer.main(testTopicName, String.valueOf(events), brokerList);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                KafkaReliableProducer.main(testTopicName, String.valueOf(events), brokerList);
            }
        }.start();

        KafkaConsumerConnector.shutdownThread.join();
        assertEquals("client produced != server offset", events * 2, sumPartitionOffset());
        assertEquals("produced != consumed", events * 2, KafkaConsumerConnector.count.intValue());
        KafkaConsumerConnector.count.set(0);
    }
}
