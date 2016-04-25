package dominus.intg.jms.kafka09;


import dominus.framework.junit.DominusJUnit4TestBase;
import kafka.admin.AdminUtils;
import kafka.tools.GetOffsetShell;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * EE: ZK Client
 * EE: create/delete test topic
 */
@ContextConfiguration(locations = "classpath:spring-container/kafka_context.xml")
public class KafkaZBaseTestCase extends DominusJUnit4TestBase {

    String brokerList;
    int replicationFactor;
    String bootstrapServers;

    //ZK
    static int zkSessionTimeout = 6000;
    static int zkConnectionTimeout = 10000;
    ZkUtils zkUtils;
    ZkClient zkClient;

    //test topic
    public static final String TEST_TOPIC_PREFIX = "page_visits_";
    String testTopicName;

    String groupId;

    @Resource(name = "kafkaProducerProps")
    Properties kafkaProducerProps;

    @Resource(name = "kafkaConsumerProps")
    Properties kafkaConsumerProps;


    @Override
    protected void doSetUp() throws Exception {
        brokerList = properties.getProperty("bootstrap.servers");
        bootstrapServers = properties.getProperty("bootstrap.servers");
        replicationFactor = Integer.valueOf(properties.getProperty("kafka.replication.factor"));
        out.println("[kafka Producer Properties]" + kafkaProducerProps.size());
        out.println("[kafka Consumer Properties]" + kafkaConsumerProps.size());
        testTopicName = TEST_TOPIC_PREFIX + new Date().getTime();
        groupId = "dominus.consumer.test." + new Date().getTime();

        // Create a ZooKeeper client
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        zkClient = new ZkClient(properties.getProperty("zkQuorum"), zkSessionTimeout, zkConnectionTimeout,
                ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(properties.getProperty("zkQuorum"));
        zkUtils = new ZkUtils(zkClient, zkConnection, false);
    }

    @Override
    protected void doTearDown() throws Exception {
        zkUtils.close();
    }

    protected boolean createTestTopic(String testTopic) throws InterruptedException {
        int numPartitions = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        AdminUtils.createTopic(zkUtils, testTopic, numPartitions, replicationFactor, new Properties());
        out.printf("Kafka Topic[%s] is created!\n", testTopic);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkUtils, testTopic));
        if (!isLocalEnvironment()) {
            out.println("Sleep 5 Seconds for Topic Initialization...");
            Thread.sleep(5 * Second);
        }
        return true;
    }

    protected boolean deleteTestTopic(String testTopic) {
        AdminUtils.deleteTopic(zkUtils, testTopicName);
        out.printf("Kafka Topic[%s] is deleted!\n", testTopicName);
        return true;
    }

    //sum all partition offset by using kafka tool(GetOffsetShell)
    protected static long sumPartitionOffset(String brokerList, String testTopicName) {
        // Tell Java to use your special stream
        preCapturedStdout();
        GetOffsetShell.main(String.format("--broker-list %s --topic %s --time -1", brokerList, testTopicName).split(" "));
        String output = capturedStdout();
        if (!StringUtils.hasText(output)) {
            println(ANSI_RED, "No output from GetOffsetShell!!!");
            return sumPartitionOffset(brokerList, testTopicName);
        }
        println(ANSI_RED, "GetOffsetShell  " + String.format("--broker-list %s --topic %s --time -1 --max-wait-ms 10000", brokerList, testTopicName));
        println(ANSI_RED, output);
        long count = 0;
        for (String partitionOffset : output.split("\n")) {
            if (StringUtils.hasText(partitionOffset) && partitionOffset.startsWith(testTopicName))
                count += Integer.valueOf(partitionOffset.split(":")[2]);
        }
        return count;
    }

    /**
     * Follow Aliyun ONS behaviours.Load props from property file or constant.
     */
    protected Producer createDefaultProducer(Properties overrideProps) {
        kafkaProducerProps.put("bootstrap.servers", bootstrapServers);
        //EE: important parameter
        kafkaProducerProps.put("acks", "1");
        //
        kafkaProducerProps.put("retries", "2");
        kafkaProducerProps.put("batch.size", "0");
        if (overrideProps != null)
            kafkaProducerProps.putAll(overrideProps);
//        kafkaProducerProps.list(out);
        Producer<String, String> producer = new KafkaProducer<>(kafkaProducerProps);
        return producer;
    }

    protected void produceTestMessage(Producer producer, String topicName, long count) throws InterruptedException, ExecutionException, TimeoutException {
        Random rnd = new Random();
        StopWatch watch = new StopWatch("[Producer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message = new ProducerRecord<String, String>(topicName, ip, info);

            RecordMetadata medadata = ((RecordMetadata) producer.send(message).get(10, TimeUnit.SECONDS));
            out.printf("[Acknowledged Message]:%s, %s, %s\n", medadata.topic(), medadata.partition(), medadata.offset());
        }
        watch.stop();
        System.out.println(watch);
    }

    protected Consumer createDefaultConsumer(String subscribeTopic, Properties overrideProps) {
        kafkaConsumerProps.put("bootstrap.servers", bootstrapServers);
        kafkaConsumerProps.put("group.id", groupId);
        kafkaConsumerProps.put("enable.auto.commit", "false");
        kafkaConsumerProps.put("auto.commit.interval.ms", "1000");
        kafkaConsumerProps.put("session.timeout.ms", "30000");
        kafkaConsumerProps.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaConsumerProps);
//        consumer.seekToBeginning(new TopicPartition(KafkaAdminTestCase.TEST_TOPIC_100K, 0));
        consumer.subscribe(Arrays.asList(subscribeTopic));

        return consumer;
    }
}