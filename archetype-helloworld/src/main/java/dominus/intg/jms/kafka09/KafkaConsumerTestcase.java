package dominus.intg.jms.kafka09;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import static org.junit.Assert.*;

public class KafkaConsumerTestcase extends KafkaZBaseTestCase {

    Consumer consumer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        consumer = this.createDefaultConsumer(KafkaAdminTestCase.TEST_TOPIC_100K, null);
    }

    @Override
    protected void doTearDown() throws Exception {
        consumer.close();
        super.doTearDown();
    }


    @Test
    public void testSimpleConsumer() {
        long count = 0;

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                out.println(record);
                count++;
            }
            consumer.commitSync();
            if (count == 10000) break;
        }
        assertEquals(100000, count);
    }

}
