package dominus.intg.jms.kafka09;

import dominus.framework.junit.DominusJUnit4TestBase;
import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.SystemTime$;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.IDefaultNameSpace;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.ZkServer;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.producer.Producer;
import org.junit.Test;
import scala.Option;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * Embedded Kafka server for unit test
 */
public class KafkaEmbeddedServerTestCase extends KafkaZBaseTestCase {

    KafkaServer _kafkaServer;
    ZkServer _zkServer;
    ZkClient _zkClient;
    ZkUtils _zkUtils;

    final String KAFKA_LOG_DIR = "/tmp/embedded_kafka/";
    final String ZK_BASE_DIR = "/tmp/zkdata";
    String TEST_TOPIC_NAME = "page_visits";


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

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
        //EE:IOPS
        props.setProperty("log.flush.interval.ms", "30000");
        props.setProperty("log.flush.scheduler.interval.ms", "30000");
        props.setProperty("log.flush.interval.messages", "30000");
        _kafkaServer = new KafkaServer(new KafkaConfig(props), SystemTime$.MODULE$, Option.apply("kafka09"));
        _kafkaServer.startup();


        _zkClient = new ZkClient("localhost:10002", dominus.intg.jms.kafka09.KafkaZBaseTestCase.zkSessionTimeout, dominus.intg.jms.kafka09.KafkaZBaseTestCase.zkConnectionTimeout,
                ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(properties.getProperty("zkQuorum"));
        _zkUtils = new ZkUtils(_zkClient, zkConnection, false);
        //EE:replication factor: 3 larger than available brokers: 1
        AdminUtils.createTopic(_zkUtils, TEST_TOPIC_NAME, 1, 1, new Properties());
        out.printf("Kafka Topic[%s] is created!\n", TEST_TOPIC_NAME);
        assertTrue("Kafka Topic[%s] does not exist!", AdminUtils.topicExists(_zkUtils, TEST_TOPIC_NAME));

        Thread.sleep(2000); //EE: wait for broker get registered in ZK
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();

        _zkClient.close();
        _kafkaServer.shutdown();
        _zkServer.shutdown();

        FileUtils.deleteDirectory(new File(KAFKA_LOG_DIR));
        FileUtils.deleteDirectory(new File(ZK_BASE_DIR));
    }

    /**
     * log.flush.interval.messages, The number of messages written to a log partition before we force an fsync on the log.
     * log.flush.scheduler.interval.ms
     * log.flush.interval.ms
     *
     * @throws InterruptedException
     */
    @Test
    public void testHandleMessageProduce() throws InterruptedException, TimeoutException, ExecutionException {

        Properties overrideProps = new Properties();
        overrideProps.setProperty("bootstrap.servers", "localhost:9090");
        Producer producer = this.createDefaultProducer(overrideProps);
        this.produceTestMessage(producer, TEST_TOPIC_NAME, 100);
        assertEquals(100, KafkaZBaseTestCase.sumPartitionOffset("localhost:9090", TEST_TOPIC_NAME));
        //produce message to it TODO
        //KafkaFastProducer.main(TEST_TOPIC_NAME, "1", "localhost:9090");


//        Thread.sleep(1 * Minute);  //sleep for debug

        //debug RequestKeys.ProduceKey
    }


}
