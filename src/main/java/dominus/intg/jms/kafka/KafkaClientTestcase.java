package dominus.intg.jms.kafka;


import dominus.PropertiesLoader;
import dominus.junit.DominusBaseTestCase;
import junit.framework.TestCase;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * EE: Consumer API, Producer API
 *
 * [External Dependencies]
 * CDH Cluster(Kafka Cluster, Zookeeper);
 * Kafka Test Topic;
 */
public class KafkaClientTestcase extends DominusBaseTestCase {


    @Override
    protected void setUp() throws Exception {

        super.setUp();
        //TODO delete topics
        //junit.framework.AssertionFailedError: expected:<4000> but was:<4969>

    }

    public void testKafkaClient() throws InterruptedException {

        Properties cdhProps = PropertiesLoader.loadCDHProperties();
        long events = Long.valueOf(cdhProps.getProperty("kafka.test.topic.msgCount"));

        KafkaConsumerConnector.main(null);
        new Thread() {
            @Override
            public void run() {
                KafkaFastProducer.main(null);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                KafkaReliableProducer.main(null);
            }
        }.start();

        KafkaConsumerConnector.shutdownThread.join();
        assertEquals(events * 2, KafkaConsumerConnector.count.intValue());
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


}
