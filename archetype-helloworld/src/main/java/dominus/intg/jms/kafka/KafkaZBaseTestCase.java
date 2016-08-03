package dominus.intg.jms.kafka;


import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.framework.junit.annotation.MessageQueueTest;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.tools.GetOffsetShell;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
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
    int numPartitions;

    //ZK
    static int zkSessionTimeout = 6000;
    static int zkConnectionTimeout = 10000;
    ZkUtils zkUtils;
    ZkClient zkClient;
    static volatile long pollTimeout = 1000;

    //test topic
    public static final String TEST_TOPIC_PREFIX = "page_visits_";
    public static final String TEST_TOPIC_100K = TEST_TOPIC_PREFIX + "100K";
    public static final String TEST_TOPIC_10K = TEST_TOPIC_PREFIX + "10K";
    String testTopicName;
    final String SEEDED_TOPIC = "page_visits_10k";
    final String COMMAND_TOPIC = "__consumer_command_request";
    final String COMMAND_REPLAY = "ALL-REPLAY";
    Producer _producer;

    String groupId;

    @Resource(name = "kafkaProducerProps")
    protected Properties kafkaProducerProps;

    @Resource(name = "kafkaConsumerProps")
    protected Properties kafkaConsumerProps;

    //partition id, messages
    Map<Integer, ArrayList<KafkaTestMessage>> testMessageMap;

    protected MessageQueueTest messageQueueAnnotation;

    String securityMechanism;


    @Override
    protected void doSetUp() throws Exception {
        brokerList = properties.getProperty("bootstrap.servers");
        bootstrapServers = properties.getProperty("bootstrap.servers");
        replicationFactor = Integer.valueOf(properties.getProperty("kafka.replication.factor"));
        numPartitions = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        securityMechanism = properties.getProperty("kafka.security");
        out.println("[kafka Producer Properties]" + kafkaProducerProps.size());
        out.println("[kafka Consumer Properties]" + kafkaConsumerProps.size());
        testTopicName = TEST_TOPIC_PREFIX + new Date().getTime();
        SimpleDateFormat format = new SimpleDateFormat("MMdd.HHmmss");

        // Create a ZooKeeper client
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        zkClient = new ZkClient(properties.getProperty("zkQuorum"), zkSessionTimeout, zkConnectionTimeout,
                ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(properties.getProperty("zkQuorum"));
        zkUtils = new ZkUtils(zkClient, zkConnection, false);

        //EE: get test method annotation
        messageQueueAnnotation = AnnotationUtils.getAnnotation(this.getClass().getMethod(this.name.getMethodName()), MessageQueueTest.class);
        if (messageQueueAnnotation != null && messageQueueAnnotation.produceTestMessage() == false) {
            testTopicName = messageQueueAnnotation.queueName();
        }
        if (messageQueueAnnotation != null && StringUtils.hasText(messageQueueAnnotation.consumerGroupId())) {
            groupId = messageQueueAnnotation.consumerGroupId();
        } else {
            groupId = "dominus.consumer.test." + format.format(new Date());
        }
        out.println("[kafka test topic name] = " + testTopicName);
        out.println("[kafka consumer group id] = " + groupId);
        out.println("[Kafka SecurityMechanism] = " + securityMechanism);

        //EE: create seeded topic.
        _producer = this.createDefaultProducer(null);
        if (!AdminUtils.topicExists(zkUtils, SEEDED_TOPIC)) {
            out.println("create seeded topic with 10000 messages");
            this.createTestTopic(SEEDED_TOPIC);
            //prepare message
            produceTestMessage(_producer, SEEDED_TOPIC, 10000);
            assertEquals(10000, sumPartitionOffset(brokerList, SEEDED_TOPIC));
        }
        if (!AdminUtils.topicExists(zkUtils, COMMAND_TOPIC)) {
            out.println("create __consumer_command_request topic...");
            this.createTestTopic(COMMAND_TOPIC, 1, 1);
        }

    }

    @Override
    protected void doTearDown() throws Exception {
        if (_producer != null) {
            _producer.close();
        }
        zkUtils.close();
    }

    protected boolean createTestTopic(String testTopic) throws InterruptedException {
        AdminUtils.createTopic(zkUtils, testTopic, numPartitions, replicationFactor, new Properties(), RackAwareMode.Disabled$.MODULE$);
        out.printf("Kafka Topic[%s] is created!\n", testTopic);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkUtils, testTopic));
        if (!isLocalEnvironment()) {
            out.println("Sleep 5 Seconds for Topic Initialization...");
            Thread.sleep(5 * Second);
        }
        return true;
    }

    protected boolean createTestTopic(String testTopic, int numPartitions, int replicationFactor) throws InterruptedException {
        AdminUtils.createTopic(zkUtils, testTopic, numPartitions, replicationFactor, new Properties(), RackAwareMode.Disabled$.MODULE$);
        out.printf("Kafka Topic[%s] is created!\n", testTopic);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkUtils, testTopic));
        if (!isLocalEnvironment()) {
            out.println("Sleep 5 Seconds for Topic Initialization...");
            Thread.sleep(5 * Second);
        }
        return true;
    }

    protected boolean deleteTestTopic(String testTopic) {
        AdminUtils.deleteTopic(zkUtils, testTopic);
        out.printf("Kafka Topic[%s] is deleted!\n", testTopic);
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
        Properties prop = new Properties();
        prop.putAll(kafkaProducerProps);
        prop.put("bootstrap.servers", bootstrapServers);
        prop.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-" + new Date().getTime());
        if (System.getProperty("java.security.auth.login.config") != null) {
            prop.put("security.protocol", "SASL_PLAINTEXT");
            prop.put("sasl.mechanism", "PLAIN");
        }
        //EE: important parameter


        if (overrideProps != null)
            prop.putAll(overrideProps);
//        kafkaProducerProps.list(out);
        Producer<String, String> producer = new KafkaProducer<>(prop);
        return producer;
    }

    protected void produceTestMessage(Producer producer, String topicName, long count) throws InterruptedException, ExecutionException, TimeoutException {

        testMessageMap = new HashMap<Integer, ArrayList<KafkaTestMessage>>();
        for (int i = 0; i < numPartitions; i++)
            testMessageMap.put(i, new ArrayList<KafkaTestMessage>((int) count));

        Random rnd = new Random();
        StopWatch watch = new StopWatch("[Producer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = String.format("nEvents=%d 192.168.2.%d", nEvents, rnd.nextInt(255));
            String info = runtime + ",www.example.com," + ip;
            ProducerRecord<String, String> message;
            if (nEvents >= count / 2)
                message = new ProducerRecord<String, String>(topicName, ip, info); // new message
            else
                message = new ProducerRecord<String, String>(topicName, null, new Date().getTime() - oneDay, ip, info); //old message


            RecordMetadata medadata = ((RecordMetadata) producer.send(message).get(10, TimeUnit.SECONDS));
            logger.info("[acknowledged message]:{}, {}, {}, {}", medadata.topic(), medadata.partition(),
                    medadata.offset(), simpleDateFormat.format(medadata.timestamp()));
            testMessageMap.get(medadata.partition()).add(new KafkaTestMessage(medadata, message));
        }
        watch.stop();
        System.out.println(watch);
    }

    protected void produceCommandMessage(Producer producer, String topicName, String receiver, String command) {
        ProducerRecord<String, String> message = new ProducerRecord<String, String>(topicName, receiver, command);
        RecordMetadata medadata = null;
        try {
            medadata = ((RecordMetadata) producer.send(message).get(10, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        logger.info("send [command message]:receiver={} command={} [acknowledged]:{}, {}, {}", receiver, command, medadata.topic(), medadata.partition(), medadata.offset());
    }

    protected Consumer createDefaultConsumer(String subscribeTopic, String consumerGroupId, Properties overrideProps, Collection<TopicPartition> assignment) {
        Properties prop = new Properties();
        prop.putAll(kafkaConsumerProps);
        prop.put("bootstrap.servers", bootstrapServers);
        prop.put("group.id", consumerGroupId);
        if (System.getProperty("java.security.auth.login.config") != null) {
            prop.put("security.protocol", "SASL_PLAINTEXT");
            prop.put("sasl.mechanism", "PLAIN");
        }
        if (overrideProps != null)
            prop.putAll(overrideProps);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
        printf(ANSI_BLUE, "create new consumer - [%s]\n", prop.getProperty("group.id"));
        ConsumerRebalanceListener rebalanceListener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                out.println("partitions revoked:" + partitions);
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                out.println("partitions assigned:" + partitions);
            }
        };
        if (assignment == null)
            consumer.subscribe(Arrays.asList(subscribeTopic), rebalanceListener); //EE: auto-balance
        else
            consumer.assign(assignment); //EE: manually assignment

        return consumer;
    }

    static class KafkaTestMessage {
        RecordMetadata medadata;
        ProducerRecord message;

        public KafkaTestMessage(RecordMetadata medadata, ProducerRecord message) {
            this.medadata = medadata;
            this.message = message;
        }
    }

    static class KafkaCommandMessage {
        String key;
        String value;
    }

}