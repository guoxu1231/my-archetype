package dominus.intg.jms.kafka09;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

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


    /**
     * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
     * EE: Consumer message from leader broker, You must keep track of the offsets in your application to know where you left off consuming.
     * (SimpleConsumer does not require zookeeper to work)
     */
    @Test
    public void testSimpleConsumer() throws UnsupportedEncodingException, InterruptedException, ExecutionException, TimeoutException {

        int kafkaPort = Integer.valueOf(properties.getProperty("kafka.seed.broker.port"));
        int partitionCount = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        final int testMsgCount = 5;

        //generate test data
        produceTestMessage(producer, testTopicName, partitionCount * testMsgCount);
        assertEquals(partitionCount * testMsgCount, sumPartitionOffset(brokerList, testTopicName));

        SimpleConsumer consumer = new SimpleConsumer(properties.getProperty("kafka.seed.broker"), kafkaPort, zkConnectionTimeout, 64 * KB, "leaderLookup");
        List<String> topics = Collections.singletonList(testTopicName);
        TopicMetadataRequest req = new TopicMetadataRequest(topics);
        TopicMetadataResponse resp = consumer.send(req);
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
            OffsetRequest leastOffsetRequest = new OffsetRequest(leastRequestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
            OffsetRequest latestOffsetRequest = new OffsetRequest(latestRequestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
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
            out.printf("[Acknowledged Message]:%s, %s, %s\n", medadata.topic(), medadata.partition(), medadata.offset());
        }
        watch.stop();
        System.out.println(watch);
        assertEquals(count, sumPartitionOffset(brokerList, testTopicName));
    }



}
