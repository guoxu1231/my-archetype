package dominus.intg.jms.rabbitmq;


import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.framework.junit.annotation.MessageQueueTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestRabbitMQClient extends DominusJUnit4TestBase {

    //abstracts the socket connection, and takes care of protocol version negotiation and authentication and so on
    Connection pubConn;
    Connection subConn;
    // most of the API for getting things done resides
    Channel pubChannel;
    Channel subChannel;
    //thread execution service for consumers on the connection
    ExecutorService executorService;
    static final String QUEUE_NAME = "TEST_RABBITMQ_QUEUE";
    static final String EXCHANGE_NAME = "TEST_RABBITMQ_EXCHANGE";
    static final String ROUTING_KEY = "TEST_ROUTING_KEY";
    MessageQueueTest messageQueueAnnotation;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        //EE: only one thread in consumer thread pool
        executorService = Executors.newFixedThreadPool(1);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("rabbitmq.host"));
        factory.setUsername(properties.getProperty("rabbitmq.user"));
        factory.setPassword(properties.getProperty("rabbitmq.user"));
//        factory.setAutomaticRecoveryEnabled(true);
        pubConn = factory.newConnection();
        //thread execution service for consumers on the connection
        subConn = factory.newConnection();
        pubChannel = pubConn.createChannel();
        subChannel = subConn.createChannel();

        //cleanup queue
        pubChannel.queueDelete(QUEUE_NAME);

        //exchanges and queues
        pubChannel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        subChannel.queueDeclare(QUEUE_NAME, true, false, false, null);
        subChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        sleep(3000);

        messageQueueAnnotation = AnnotationUtils.getAnnotation(this.getClass().getMethod(this.name.getMethodName()), MessageQueueTest.class);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        pubChannel.close();
        subChannel.close();
        pubConn.close();
        subConn.close();
        executorService.shutdown();
    }

    @MessageQueueTest(count = 10000)
    @Test
    public void testBasicPubSub() throws IOException, InterruptedException {

        int count = messageQueueAnnotation.count();
        CountDownLatch latch = new CountDownLatch(count);

        produceTestMessage(count);

        pubChannel.basicConsume(QUEUE_NAME, false, "myConsumerTag", new DefaultConsumer(pubChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                long deliveryTag = envelope.getDeliveryTag();
                logger.info(" [x] Received {} Tag {}'", message, deliveryTag);

                pubChannel.basicAck(deliveryTag, false);
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.MINUTES));
    }

    @MessageQueueTest(count = 100000)
    @Ignore
    public void testSlowConsumer() throws IOException, InterruptedException, TimeoutException {

        int count = messageQueueAnnotation.count();
        CountDownLatch latch = new CountDownLatch(1);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("rabbitmq.host"));
        factory.setUsername(properties.getProperty("rabbitmq.user"));
        factory.setPassword(properties.getProperty("rabbitmq.user"));
        factory.setExceptionHandler(new DefaultExceptionHandler() {
            @Override
            public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {
                out.printf("%s : %s\n", exception.getClass().getName(), exception.getMessage());
                assertTrue(exception instanceof java.net.SocketException || exception instanceof AlreadyClosedException);
                latch.countDown();
            }
        });
        Connection testConn = factory.newConnection(executorService);
        Channel testChannel = testConn.createChannel();

        produceTestMessage(count);
        //EE: default prefetch is unlimited. Reduce prefetch count to eliminate connection timeout,
//        testChannel.basicQos(10);

        testChannel.basicConsume(QUEUE_NAME, false, "myConsumerTag", new DefaultConsumer(testChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                long deliveryTag = envelope.getDeliveryTag();
                out.printf(" [x] Received %s Tag %s\n", message, deliveryTag);
                testChannel.basicAck(deliveryTag, false);
                sleep(1000);
            }
        });
        assertTrue(latch.await(10, TimeUnit.MINUTES));
        try {
            testChannel.close();
            testConn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        pubChannel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, info.getBytes());
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
