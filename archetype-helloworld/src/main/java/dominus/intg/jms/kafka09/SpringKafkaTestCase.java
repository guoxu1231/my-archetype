package dominus.intg.jms.kafka09;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@ContextConfiguration(locations = "classpath:spring-container/kafka_context.xml")
public class SpringKafkaTestCase extends KafkaZBaseTestCase {

    @Resource(name = "kafkaProducerProps")
    Properties kafkaProducerProps;

    @Resource(name = "kafkaConsumerProps")
    Properties kafkaConsumerProps;

    KafkaTemplate<String, String> template;
    ProducerFactory<String, String> producerFactory;
    Producer<String, String> producer;

    KafkaMessageListenerContainer<String, String> consumer;
    final CountDownLatch latch = new CountDownLatch(1000);

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        //EE: plain Java to create kafka producer
        Map<String, Object> pProps = new HashMap<>();

        //load from default config template
        pProps.putAll((Map) kafkaProducerProps);

        //override props
        pProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
        pProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        pProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        //EE:singleton shared Producer instance.
        // Freed from the external Producer.close() invocation with the internal wrapper.
        // The real Producer.close() is called on the target Producer during the Lifecycle.stop() or DisposableBean.destroy().
        producerFactory = new DefaultKafkaProducerFactory<String, String>(pProps);
        template = new KafkaTemplate<>(producerFactory);
        producer = producerFactory.createProducer();


        //EE: plain Java to create kafka consumer(MessageListener)
        Map<String, Object> cProps = new HashMap<>();


        //load from default config template
        cProps.putAll((Map) kafkaConsumerProps);
        cProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
        cProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        cProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        cProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        DefaultKafkaConsumerFactory<String, String> cf =
                new DefaultKafkaConsumerFactory<String, String>(cProps);
        consumer = new KafkaMessageListenerContainer<>(cf, testTopicName);


        consumer.setMessageListener(new MessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> message) {
                logger.info("received: " + message);
                latch.countDown();
            }
        });

        this.createTestTopic(testTopicName);

    }

    @Override
    protected void doTearDown() throws Exception {
        ((DefaultKafkaProducerFactory) producerFactory).destroy();
        this.deleteTestTopic(testTopicName);
        consumer.stop();

        super.doTearDown();
    }

    @Test
    public void testProduceMessage() throws InterruptedException, ExecutionException, TimeoutException {
        produceTestMessage(producer, testTopicName, 100);
        assertEquals(sumPartitionOffset(brokerList, testTopicName), 100);
    }

    @Test
    public void testConsumerMessage() throws InterruptedException, ExecutionException, TimeoutException {
        produceTestMessage(producer, testTopicName, 1000);
        consumer.start(); //EE: unlike mq, non-blocking call
        latch.await();
        assertEquals(0, latch.getCount());
    }
}
