package dominus.intg.jms.kafka;


import dominus.intg.jms.kafka.sdk.producer.KafkaFastProducer;
import org.junit.Test;
import org.springframework.test.annotation.Repeat;

import static org.junit.Assert.assertEquals;

public class KafkaPerformanceTestCase extends KafkaZBaseTestCase {

    @Test
    @Repeat(50)
    public void testReliableMessagePublish() throws InterruptedException {
        long events = Long.valueOf(properties.getProperty("kafka.perf.topic.msgCount"));
        //KafkaFastProducer  KafkaReliableProducer
        KafkaFastProducer.main(testTopicName, String.valueOf(events), brokerList);
        Thread.sleep(5000);//EE: wait for message got flushed in broker
        //total partition offset should be equal with events count.
        assertEquals(events, sumPartitionOffset(brokerList, testTopicName));
        println(ANSI_RED, "assertEquals passed");
    }

}
