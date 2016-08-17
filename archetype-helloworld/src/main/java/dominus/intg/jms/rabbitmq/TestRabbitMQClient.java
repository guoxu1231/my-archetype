package dominus.intg.jms.rabbitmq;


import com.rabbitmq.client.*;
import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.framework.junit.annotation.MessageQueueTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;


public class TestRabbitMQClient extends DominusJUnit4TestBase {

    //abstracts the socket connection, and takes care of protocol version negotiation and authentication and so on
    Connection conn;
    // most of the API for getting things done resides
    Channel channel;
    static final String QUEUE_NAME = "TEST_RABBITMQ_QUEUE";
    static final String EXCHANGE_NAME = "TEST_RABBITMQ_EXCHANGE";
    static final String ROUTING_KEY = "TEST_ROUTING_KEY";
    MessageQueueTest messageQueueAnnotation;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        messageQueueAnnotation = AnnotationUtils.getAnnotation(this.getClass().getMethod(this.name.getMethodName()), MessageQueueTest.class);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("rabbitmq.host"));
        factory.setUsername(properties.getProperty("rabbitmq.user"));
        factory.setPassword(properties.getProperty("rabbitmq.user"));
//        factory.setAutomaticRecoveryEnabled(true);
        conn = factory.newConnection();
        channel = conn.createChannel();
        //
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        channel.close();
        conn.close();
    }

    @MessageQueueTest(count = 10000)
    @Test
    public void testBasicPubSub() throws IOException, InterruptedException {

        int count = messageQueueAnnotation.count();
        CountDownLatch latch = new CountDownLatch(count);

        produceTestMessage(count);

        channel.basicConsume(QUEUE_NAME, false, "myConsumerTag", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                long deliveryTag = envelope.getDeliveryTag();
                logger.info(" [x] Received {} Tag {}'", message, deliveryTag);

                channel.basicAck(deliveryTag, false);
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.MINUTES));
    }

    public void testSlowConsumer() {

    }

    public void testCompetingConsumer() {

    }

    void produceTestMessage(int msgCount) {
        Thread t = new Thread() {
            @Override
            public void run() {
                for (long nEvents = 0; nEvents < msgCount; nEvents++) {
                    long runtime = new Date().getTime();
                    String ip = "192.168.2." + random.nextInt(255);
                    String info = runtime + ",www.example.com," + ip;
                    try {
                        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, info.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.setName("test-pub-thread");
        t.start();
    }
}
