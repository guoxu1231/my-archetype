package dominus.intg.jms.kafka;


import dominus.PropertiesLoader;
import dominus.framework.junit.DominusBaseTestCase;
import kafka.admin.AdminUtils;
import kafka.tools.GetOffsetShell;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Properties;

/**
 * EE: Consumer API, Producer API, Admin API
 * <p/>
 * [External Dependencies]
 * CDH Cluster(Kafka Cluster, Zookeeper);
 * Kafka Test Topic;
 */
public class KafkaClientTestcase extends DominusBaseTestCase {

    int sessionTimeoutMs = 10000;
    int connectionTimeoutMs = 10000;
    int replicationFactor = 3;
    final String topicName = "page_visits_" + new Date().getTime();
    ZkClient zkClient;
    String brokerList;

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        // Create a ZooKeeper client
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        zkClient = new ZkClient(properties.getProperty("zkQuorum"), sessionTimeoutMs, connectionTimeoutMs,
                ZKStringSerializer$.MODULE$);

//        topicName = properties.getProperty("kafka.test.topic");
//        if (AdminUtils.topicExists(zkClient, topicName)) {
//            AdminUtils.deleteTopic(zkClient, topicName);
//            out.printf("Kafka Topic[%s] is deleted!\n", topicName);
//        }
        int numPartitions = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        AdminUtils.createTopic(zkClient, topicName, numPartitions, replicationFactor, new Properties());
        out.printf("Kafka Topic[%s] is created!\n", topicName);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkClient, topicName));

        brokerList = properties.getProperty("bootstrap.servers");
    }


    public void testRoundRobinPartitioner() {
        long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));
        KafkaReliableProducer.main(topicName, String.valueOf(events), brokerList);
        //total partition offset should be equal with events count.
        assertEquals(events, sumPartitionOffset());
    }


    public void testKafkaClusterStatus() {

    }


    public void testKafkaClient() throws InterruptedException {

        final long events = Long.valueOf(properties.getProperty("kafka.test.topic.msgCount"));

        KafkaConsumerConnector.main(topicName);
        //junit.framework.AssertionFailedError:Expected :500 Actual :244
        Thread.sleep(5000);
        new Thread() {
            @Override
            public void run() {
                KafkaFastProducer.main(topicName, String.valueOf(events), brokerList);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                KafkaReliableProducer.main(topicName, String.valueOf(events), brokerList);
            }
        }.start();

        KafkaConsumerConnector.shutdownThread.join();
        //client produced = server offset
        assertEquals(events * 2, sumPartitionOffset());
        //produced = consumed
        assertEquals(events * 2, KafkaConsumerConnector.count.intValue());
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AdminUtils.deleteTopic(zkClient, topicName);
        out.printf("Kafka Topic[%s] is deleted!\n", topicName);
    }

    //sum all partition offset by using kafka tool(GetOffsetShell)
    private long sumPartitionOffset() {
        // Tell Java to use your special stream
        System.setOut(ps);
        GetOffsetShell.main(String.format("--broker-list %s --topic %s --time -1", brokerList, topicName).split(" "));
        String output = capturedStdout();
        out.println(output);
        long count = 0;
        for (String partitionOffset : output.split("\n")) {
            if (StringUtils.hasText(partitionOffset) && partitionOffset.startsWith(topicName))
                count += Integer.valueOf(partitionOffset.split(":")[2]);
        }
        return count;
    }


}
