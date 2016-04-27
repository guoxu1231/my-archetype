package dominus.intg.jms.kafka;


import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.SystemTime$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static scala.collection.JavaConversions.seqAsJavaList;

public class KafkaAdminTestCase extends KafkaZBaseTestCase {

    String ConsumersPath = "/consumers";
    String BrokerIdsPath = "/brokers/ids";
    String BrokerTopicsPath = "/brokers/topics";
    String TopicConfigPath = "/config/topics";
    String TopicConfigChangesPath = "/config/changes";
    String ControllerPath = "/controller";
    String ControllerEpochPath = "/controller_epoch";
    String ReassignPartitionsPath = "/admin/reassign_partitions";
    String DeleteTopicsPath = "/admin/delete_topics";
    String PreferredReplicaLeaderElectionPath = "/admin/preferred_replica_election";

    /**
     * EE: delete kafka topic manually
     * Stop the Apache Kafka daemon
     * Delete the topic data folder: rm -rf /tmp/kafka-logs/MyTopic-0
     * Delete the topic metadata: zkCli.sh then rmr /brokers/MyTopic
     * Start the Apache Kafka daemon
     */
    @Test
    public void testDeleteTestTopics() {
        List<String> topics = seqAsJavaList(ZkUtils.getAllTopics(zkClient));
        out.println("All Topics:" + topics);
        for (String topic : topics) {
            if (topic.startsWith(TEST_TOPIC_PREFIX)) {
                try {
                    AdminUtils.deleteTopic(zkClient, topic);
                } catch (ZkNodeExistsException e) {
                    out.println(e);
                }
            }
        }
    }

    //TODO deletion success but zknode still existed.
    @Test
    public void testDeleteTopicMetadata() {
        List<String> topics = seqAsJavaList(ZkUtils.getChildrenParentMayNotExist(zkClient, BrokerTopicsPath));
        out.println(topics);
        for (String topic : topics) {
            if (topic.startsWith(TEST_TOPIC_PREFIX)) {
                ZkUtils.deletePathRecursive(zkClient, BrokerTopicsPath + topic);
                out.println("/brokers/topics/" + topic + " is deleted!");
            }
        }
    }

    /**
     * kafka.server.KafkaHealthcheck registers the broker in zookeeper to allow other brokers and consumers to detect failures.
     * If we register in zk we are healthy, otherwise we are dead.
     *
     * @throws InterruptedException
     */
    @Test
    public void testWatchKafkaBrokerIds() throws InterruptedException {

        printf(ANSI_RED, "Current Broker List:%s\n", ZkUtils.getSortedBrokerList(zkClient));

        final String embeddedBrokerId = "888";
        //startup kafka server and register it to ZK server.
        Thread t = new Thread() {
            @Override
            public void run() {
                Properties props = new Properties();
                props.setProperty("hostname", "localhost");
                props.setProperty("port", "9090");
                props.setProperty("broker.id", embeddedBrokerId);
                props.setProperty("log.dir", "/tmp/embeddedkafka/");
                props.setProperty("enable.zookeeper", "false");
                props.setProperty("zookeeper.connect", properties.getProperty("zkQuorum"));
                KafkaServer server = new KafkaServer(new KafkaConfig(props), SystemTime$.MODULE$);
                server.startup();
                server.shutdown();
            }
        };
        t.start();
        final List<List<String>> zkEvent = new ArrayList<List<String>>();
        zkClient.subscribeChildChanges(BrokerIdsPath, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                printf(ANSI_RED, "[%s] Current Childs: %s\n", parentPath, StringUtils.arrayToCommaDelimitedString(currentChilds.toArray()));
                zkEvent.add(currentChilds);
            }
        });
        t.join();
        Thread.sleep(1500);//waiting for shutdown event
        Assert.assertEquals("startup and shutdown event", 2, zkEvent.size());
        Assert.assertTrue("embeddedBrokerId!=888", CollectionUtils.subtract(zkEvent.get(0), zkEvent.get(1)).contains(embeddedBrokerId));
    }

    @Test
    public void testNull() {
        printf(ANSI_BLUE, "TEst\n");
    }

    @Override
    protected boolean createTestTopic() {
        return false;
    }


}
