package dominus.intg.jms.kafka;


import dominus.intg.jms.kafka.consumer.KafkaConsumerConnector;
import dominus.intg.jms.kafka.producer.KafkaFastProducer;
import dominus.intg.jms.kafka.producer.KafkaReliableProducer;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.junit.Test;

import static org.junit.Assert.*;

import org.springframework.test.annotation.Repeat;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void testRoundRobinPartitioner() throws InterruptedException {
        long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));
        KafkaFastProducer.main(testTopicName, String.valueOf(events), brokerList);
        Thread.sleep(5000);
        //total partition offset should be equal with events count.
        assertEquals(events, sumPartitionOffset(brokerList, testTopicName));
    }

    /**
     * Test producer.type = async/sync
     *
     * @throws InterruptedException
     */
    @Test
    @Repeat(2)
    public void testKafkaProducer() throws InterruptedException {

        final long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));

        KafkaConsumerConnector.main(testTopicName, groupId, properties.getProperty("zkQuorum"),
                properties.getProperty("kafka.test.topic.partition"));
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
        assertEquals("client produced != server offset", events * 2, sumPartitionOffset(brokerList, testTopicName));
        assertEquals("produced != consumed", events * 2, KafkaConsumerConnector.count.intValue());
        KafkaConsumerConnector.count.set(0);
    }

    /**
     * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
     * EE: Consumer message from leader broker, You must keep track of the offsets in your application to know where you left off consuming.
     * (SimpleConsumer does not require zookeeper to work)
     */
    @Test
    public void testSimpleConsumer() throws UnsupportedEncodingException {

        int kafkaPort = Integer.valueOf(properties.getProperty("kafka.seed.broker.port"));
        int partitionCount = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        final int testMsgCount = 5;

        //generate test data
        KafkaReliableProducer.main(testTopicName, String.valueOf(partitionCount * testMsgCount), brokerList);
        assertEquals(partitionCount * testMsgCount, sumPartitionOffset(brokerList, testTopicName));

        SimpleConsumer consumer = new SimpleConsumer(properties.getProperty("kafka.seed.broker"), kafkaPort, zkConnectionTimeout, 64 * KB, "leaderLookup");
        List<String> topics = Collections.singletonList(testTopicName);
        TopicMetadataRequest req = new TopicMetadataRequest(topics);
        kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
        //EE: find TopicMetadata from seed kafka server
        List<TopicMetadata> metaData = resp.topicsMetadata();
        assertTrue(metaData.size() == 1);
        List<PartitionMetadata> partitionList = metaData.get(0).partitionsMetadata();
        assertTrue(partitionList.size() >= 1);
        for (PartitionMetadata partition : partitionList) {
            println(ANSI_RED, partition);
            String leadBroker = partition.leader().host();
            String clientName = "Client_" + testTopicName + "_" + partition.partitionId();
            SimpleConsumer partitionConsumer = new SimpleConsumer(leadBroker, kafkaPort, zkConnectionTimeout, 64 * KB, clientName);
            TopicAndPartition topicAndPartition = new TopicAndPartition(testTopicName, partition.partitionId());
            Map<TopicAndPartition, PartitionOffsetRequestInfo> leastRequestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
            leastRequestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.EarliestTime(), 1));
            Map<TopicAndPartition, PartitionOffsetRequestInfo> latestRequestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
            latestRequestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.LatestTime(), Integer.MAX_VALUE));
            //EE: build OffsetRequest and send it to leadBroker
            kafka.javaapi.OffsetRequest leastOffsetRequest = new kafka.javaapi.OffsetRequest(leastRequestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
            kafka.javaapi.OffsetRequest latestOffsetRequest = new kafka.javaapi.OffsetRequest(latestRequestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
            OffsetResponse leastResponse = partitionConsumer.getOffsetsBefore(leastOffsetRequest);
            OffsetResponse latestResponse = partitionConsumer.getOffsetsBefore(latestOffsetRequest);
            assertFalse("Error fetching data Offset Data the Broker. Reason: " + leastResponse.errorCode(testTopicName, partition.partitionId()), leastResponse.hasError());
            assertFalse("Error fetching data Offset Data the Broker. Reason: " + latestResponse.errorCode(testTopicName, partition.partitionId()), latestResponse.hasError());
            long leastOffset = leastResponse.offsets(testTopicName, partition.partitionId())[0];
            long latestOffset = latestResponse.offsets(testTopicName, partition.partitionId())[0];
            assertEquals(testMsgCount, latestOffset);
            printf(ANSI_RED, "partition [%d] least offset is %d  latest offset is %d\n", partition.partitionId(), leastOffset, latestOffset);

            FetchRequest fetchRequest = new FetchRequestBuilder()
                    .clientId(clientName)
                    .addFetch(testTopicName, partition.partitionId(), leastOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                    .build();
            FetchResponse fetchResponse = partitionConsumer.fetch(fetchRequest);
            //EE: TODO find new leader when met error
            assertFalse("Error fetching data from the Broker:" + leadBroker + " Reason: " + fetchResponse.errorCode(testTopicName, partition.partitionId()), fetchResponse.hasError());

            //EE: read message from least offset
            long readOffset = leastOffset;
            long numRead = 0;
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(testTopicName, partition.partitionId())) {
                ByteBuffer payload = messageAndOffset.message().payload();
                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                println(ANSI_YELLOW, String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
                numRead++;
            }
            assertEquals("fetch message count does not match test message count", testMsgCount, numRead);
            if (partitionConsumer != null) partitionConsumer.close();
        }
        if (consumer != null) consumer.close();
    }


}
