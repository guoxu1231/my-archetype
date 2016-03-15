package dominus.intg.jms.kafka;


import dominus.PropertiesLoader;
import dominus.framework.junit.DominusBaseTestCase;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;

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
    final String topicName = "page_visits_1458017754893";
    ZkClient zkClient;

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        //TODO delete topics
        //junit.framework.AssertionFailedError: expected:<4000> but was:<4969>

        // Create a ZooKeeper client
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        zkClient = new ZkClient(properties.getProperty("zkQuorum"), sessionTimeoutMs, connectionTimeoutMs,
                ZKStringSerializer$.MODULE$);

//        topicName = properties.getProperty("kafka.test.topic");
        int numPartitions = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
        if (AdminUtils.topicExists(zkClient, topicName)) {
            AdminUtils.deleteTopic(zkClient, topicName);
            out.printf("Kafka Topic[%s] is deleted!\n", topicName);
        }
        AdminUtils.createTopic(zkClient, topicName, numPartitions, replicationFactor, new Properties());
        out.printf("Kafka Topic[%s] is created!\n", topicName);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkClient, topicName));
    }

    public void testKafkaClient() throws InterruptedException {

        Properties cdhProps = PropertiesLoader.loadCDHProperties();
        long events = Long.valueOf(cdhProps.getProperty("kafka.test.topic.msgCount"));

        KafkaConsumerConnector.main(topicName);
        //junit.framework.AssertionFailedError:Expected :500 Actual :244
        Thread.sleep(5000);
        new Thread() {
            @Override
            public void run() {
                KafkaFastProducer.main(topicName);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                KafkaReliableProducer.main(topicName);
            }
        }.start();

        KafkaConsumerConnector.shutdownThread.join();
        assertEquals(events * 2, KafkaConsumerConnector.count.intValue());
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AdminUtils.deleteTopic(zkClient, topicName);
        out.printf("Kafka Topic[%s] is deleted!\n", topicName);
    }


}
