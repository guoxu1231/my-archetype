package dominus.intg.jms.kafka09;


import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.SystemTime$;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.producer.Producer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StringUtils;
import scala.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
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
    @Ignore
    public void testDeleteTestTopics() {
        List<String> topics = seqAsJavaList(zkUtils.getAllTopics());
        out.println("All Topics:" + topics);
        for (String topic : topics) {
            if (topic.startsWith(TEST_TOPIC_PREFIX)) {
                try {
                    AdminUtils.deleteTopic(zkUtils, topic);
                } catch (ZkNodeExistsException e) {
                    out.println(e);
                }
            }
        }
    }

    //TODO deletion success but zknode still existed.
    @Ignore
    public void testDeleteTopicMetadata() {
        List<String> topics = seqAsJavaList(zkUtils.getChildrenParentMayNotExist(BrokerTopicsPath));
        out.println(topics);
        for (String topic : topics) {
            if (topic.startsWith(TEST_TOPIC_PREFIX)) {
                zkUtils.deletePathRecursive(BrokerTopicsPath + topic);
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

        printf(ANSI_RED, "Current Broker List:%s\n", zkUtils.getSortedBrokerList());

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
                KafkaServer server = new KafkaServer(new KafkaConfig(props), SystemTime$.MODULE$, Option.apply("kafka09"));
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



    /**
     * DO NOT DELETE 100K TEST TOPIC
     */
    @Ignore
    public void testCreate10kTopic() throws InterruptedException, ExecutionException, TimeoutException {
        this.createTestTopic(TEST_TOPIC_100K);
        Producer producer = this.createDefaultProducer(null);
        produceTestMessage(producer, TEST_TOPIC_100K, 100000L);
        producer.close();
        Thread.sleep(5 * Second);
        assertEquals(100000L, sumPartitionOffset(brokerList, TEST_TOPIC_100K));
    }


}
