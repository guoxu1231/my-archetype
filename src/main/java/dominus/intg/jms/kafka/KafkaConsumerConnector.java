package dominus.intg.jms.kafka;

import dominus.PropertiesLoader;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Old High Level Consumer API(before 0.9)
 * From Consumer Group Example
 * https://cwiki.apache.org/confluence/display/KAFKA/Consumer+Group+Example
 * <p/>
 * First thing to know is that the High Level Consumer stores the last offset read from a specific partition in ZooKeeper;
 * This offset is stored based on the name provided to Kafka when the process starts. This name is referred to as the Consumer Group.
 */
public class KafkaConsumerConnector {

    protected static ExecutorService executor;
    protected static AtomicInteger count = new AtomicInteger(0);
    protected static Integer exitCount;
    protected static Thread shutdownThread;

    public static void main(String[] args) throws InterruptedException {

        Properties cdhProps = PropertiesLoader.loadCDHProperties();
        Properties props = new Properties();
        props.put("zookeeper.connect", cdhProps.getProperty("zkQuorum"));
        props.put("group.id", "dominus.kafka.consumer.test");
        props.put("zookeeper.session.timeout.ms", "10000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        //EE: exit condition
        /**
         * kafka.consumer.ConsumerTimeoutException
         at kafka.consumer.ConsumerIterator.makeNext(ConsumerIterator.scala:69)
         at kafka.consumer.ConsumerIterator.makeNext(ConsumerIterator.scala:33)
         at kafka.utils.IteratorTemplate.maybeComputeNext(IteratorTemplate.scala:66)
         at kafka.utils.IteratorTemplate.hasNext(IteratorTemplate.scala:58)
         at dominus.intg.jms.kafka.KafkaConsumerConnector$MessageHandler.run(KafkaConsumerConnector.java:79)
         */
        props.put("consumer.timeout.ms", "10000");
        ConsumerConfig consumerConfig = new ConsumerConfig(props);

        //variables
        int numThreads = new Integer(cdhProps.getProperty("kafka.test.topic.partition"));
        String topic = cdhProps.getProperty("kafka.test.topic");
        Decoder decoder = new StringDecoder(consumerConfig.props());


        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, numThreads);
        final ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, decoder, decoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(topic);
        executor = Executors.newFixedThreadPool(numThreads);

        // now create an object to consume the messages
        final List<Future<?>> futures = new ArrayList<Future<?>>();
        for (final KafkaStream stream : streams) {
            futures.add(executor.submit(new MessageHandler(stream)));
        }

        //Non-blocking shutdown
        shutdownThread = new Thread() {
            @Override
            public void run() {
                //blocking call
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        futures.get(i).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                //EE clean shutdown
                consumer.shutdown();
                executor.shutdown();
            }
        };
        shutdownThread.start();
    }

    public static class MessageHandler implements Runnable {
        private KafkaStream m_stream;

        public MessageHandler(KafkaStream a_stream) {
            m_stream = a_stream;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
            try {
                while (it.hasNext()) {
                    System.out.println("[Message Consumer] [Thread] " + Thread.currentThread().getName() + ": " + it.next().message());
                    count.incrementAndGet();
                }
            } catch (ConsumerTimeoutException e) {
                e.printStackTrace();
                System.out.println(" No message is available for consumption after the specified interval: " + Thread.currentThread().getName());
                return;
            }
        }
    }

}
