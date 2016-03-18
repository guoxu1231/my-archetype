package dominus.intg.jms.kafka;

import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.jms.kafka.producer.KafkaFastProducer;
import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.SystemTime$;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.IDefaultNameSpace;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkServer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * Embedded Kafka server for unit test
 */
public class KafkaServerTestCase extends DominusJUnit4TestBase {

    KafkaServer _kafkaServer;
    ZkServer _zkServer;
    ZkClient _zkClient;

    final String KAFKA_LOG_DIR = "/tmp/embedded_kafka/";
    final String ZK_BASE_DIR = "/tmp/zkdata";
    String TEST_TOPIC_NAME = "page_visits";


    @Override
    protected void doSetUp() throws Exception {

        //EE: startup local zkServer
        //zkclient/src/test/java/org/I0Itec/zkclient/testutil/ZkTestSystem.java
        FileUtils.deleteDirectory(new File(ZK_BASE_DIR));
        String dataDir = ZK_BASE_DIR + "/data";
        String logDir = ZK_BASE_DIR + "/log";
        _zkServer = new ZkServer(dataDir, logDir, mock(IDefaultNameSpace.class), 10002);
        _zkServer.start();

        //EE: startup embedded kafka server
        FileUtils.deleteDirectory(new File(KAFKA_LOG_DIR));
        Properties props = new Properties();
        props.setProperty("hostname", "localhost");
        props.setProperty("port", "9090");
        props.setProperty("broker.id", "888");
        props.setProperty("log.dir", KAFKA_LOG_DIR);
//        props.setProperty("enable.zookeeper", "false"); EE: obsolete in 0.7
        props.setProperty("zookeeper.connect", "localhost:10002");
        _kafkaServer = new KafkaServer(new KafkaConfig(props), SystemTime$.MODULE$);
        _kafkaServer.startup();


        _zkClient = new ZkClient("localhost:10002", KafkaZBaseTestCase.zkSessionTimeout, KafkaZBaseTestCase.zkConnectionTimeout,
                ZKStringSerializer$.MODULE$);
        //EE:replication factor: 3 larger than available brokers: 1
        AdminUtils.createTopic(_zkClient, TEST_TOPIC_NAME, 1, 1, new Properties());
        out.printf("Kafka Topic[%s] is created!\n", TEST_TOPIC_NAME);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(_zkClient, TEST_TOPIC_NAME));

        Thread.sleep(2000); //EE: wait for broker get registered in ZK
    }

    @Override
    protected void doTearDown() throws Exception {
        _zkClient.close();
        _kafkaServer.shutdown();
        _zkServer.shutdown();

        FileUtils.deleteDirectory(new File(KAFKA_LOG_DIR));
        FileUtils.deleteDirectory(new File(ZK_BASE_DIR));
    }

    @Test
    public void testHandleProducer() throws InterruptedException {
        //produce message to it
        KafkaFastProducer.main(TEST_TOPIC_NAME, "1", "localhost:9090");
//        Thread.sleep(1 * Minute);  //sleep for debug
        assertEquals(1, KafkaZBaseTestCase.sumPartitionOffset("localhost:9090", TEST_TOPIC_NAME));

        //debug RequestKeys.ProduceKey
    }


}
