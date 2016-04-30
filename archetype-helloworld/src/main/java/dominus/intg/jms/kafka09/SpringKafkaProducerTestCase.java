package dominus.intg.jms.kafka09;


import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@ContextConfiguration(locations = "classpath:spring-container/kafka_context.xml")
public class SpringKafkaProducerTestCase extends KafkaZBaseTestCase {

    @Resource(name = "kafkaProducerProps")
    Properties kafkaProducerProps;

    KafkaTemplate<String, String> template;
    ProducerFactory<String, String> producerFactory;
    Producer<String, String> producer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        //EE: plain Java to create kafka producer
        Map<String, Object> props = new HashMap<>();

        //load from default config template
        props.putAll((Map) kafkaProducerProps);

        //override props
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        //EE:singleton shared Producer instance.
        // Freed from the external Producer.close() invocation with the internal wrapper.
        // The real Producer.close() is called on the target Producer during the Lifecycle.stop() or DisposableBean.destroy().
        producerFactory = new DefaultKafkaProducerFactory<String, String>(props);
        template = new KafkaTemplate<>(producerFactory);
        producer = producerFactory.createProducer();

        this.createTestTopic(testTopicName);

    }

    @Override
    protected void doTearDown() throws Exception {
        ((DefaultKafkaProducerFactory) producerFactory).destroy();
        this.deleteTestTopic(testTopicName);

        super.doTearDown();
    }

    @Test
    public void testProduceMessage() throws InterruptedException, ExecutionException, TimeoutException {
        produceTestMessage(producer, testTopicName, 100);
        assertEquals(sumPartitionOffset(brokerList, testTopicName), 100);
    }

}
