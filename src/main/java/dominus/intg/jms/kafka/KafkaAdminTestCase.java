package dominus.intg.jms.kafka;


import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.junit.Test;

import java.util.List;

import static scala.collection.JavaConversions.seqAsJavaList;

public class KafkaAdminTestCase extends KafkaZBaseTestCase {

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

    @Test
    public void testDeleteTopicMetadata() {
        List<String> topicsPath = seqAsJavaList(ZkUtils.getChildrenParentMayNotExist(zkClient, "/brokers/topics"));
        out.println(topicsPath);
        for (String path : topicsPath) {
            if (path.startsWith(TEST_TOPIC_PREFIX)) {
                ZkUtils.deletePathRecursive(zkClient, "/brokers/topics/" + path);
                out.println("/brokers/topics/" + path + " is deleted!");
            }
        }
    }

    @Test
    public void testKafkaClusterStatus() {

    }

    @Override
    protected boolean createTestTopic() {
        return false;
    }
}
