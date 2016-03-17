package dominus.intg.jms.kafka;


import dominus.framework.junit.DominusJUnit4TestBase;
import kafka.admin.AdminUtils;
import kafka.tools.GetOffsetShell;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * EE: ZK Client
 * EE: create/delete test topic
 */
public class KafkaZBaseTestCase extends DominusJUnit4TestBase {

    String brokerList;
    int replicationFactor = 3;

    //ZK
    int zkSessionTimeout = 6000;
    int zkConnectionTimeout = 10000;
    ZkClient zkClient;

    //test topic
    String TEST_TOPIC_PREFIX = "page_visits_";
    String testTopicName;

    String groupId;


    @Override
    protected void doSetUp() throws Exception {
        brokerList = properties.getProperty("bootstrap.servers");

        // Create a ZooKeeper client
        // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
        // createTopic() will only seem to work (it will return without error).  The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        zkClient = new ZkClient(properties.getProperty("zkQuorum"), zkSessionTimeout, zkConnectionTimeout,
                ZKStringSerializer$.MODULE$);

        testTopicName = TEST_TOPIC_PREFIX + new Date().getTime();
        groupId = "dominus.consumer.test." + new Date().getTime();

        if (createTestTopic()) {
            int numPartitions = Integer.valueOf(properties.getProperty("kafka.test.topic.partition"));
            AdminUtils.createTopic(zkClient, testTopicName, numPartitions, replicationFactor, new Properties());
            out.printf("Kafka Topic[%s] is created!\n", testTopicName);
            assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(zkClient, testTopicName));
        }


    }

    @Override
    protected void doTearDown() throws Exception {
        if (createTestTopic()) {
            AdminUtils.deleteTopic(zkClient, testTopicName);
            out.printf("Kafka Topic[%s] is deleted!\n", testTopicName);
        }
        zkClient.close();
    }


    //sum all partition offset by using kafka tool(GetOffsetShell)
    protected long sumPartitionOffset() {
        // Tell Java to use your special stream
        System.setOut(ps);
        GetOffsetShell.main(String.format("--broker-list %s --topic %s --time -1", brokerList, testTopicName).split(" "));
        String output = capturedStdout();
        out.println(output);
        long count = 0;
        for (String partitionOffset : output.split("\n")) {
            if (StringUtils.hasText(partitionOffset) && partitionOffset.startsWith(testTopicName))
                count += Integer.valueOf(partitionOffset.split(":")[2]);
        }
        return count;
    }

    protected boolean createTestTopic() {
        return true;
    }

}
