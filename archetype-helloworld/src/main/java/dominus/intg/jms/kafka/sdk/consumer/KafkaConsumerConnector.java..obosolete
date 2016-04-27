package dominus.intg.jms.kafka.sdk.consumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
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
    public static AtomicInteger count = new AtomicInteger(0);
    protected static Integer exitCount;
    public static Thread shutdownThread;
    static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConnector.class);

    public static void main(String... args) throws InterruptedException {

        Properties props = new Properties();
        props.put("zookeeper.connect", args[2]);
        props.put("group.id", args[1]);
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
         at dominus.intg.jms.kafka.sdk.consumer.KafkaConsumerConnector$MessageHandler.run(KafkaConsumerConnector.java:79)
         */
        props.put("consumer.timeout.ms", "30000");
        ConsumerConfig consumerConfig = new ConsumerConfig(props);

        //variables
        int numThreads = new Integer(args[3]);
        String topic = args[0];
        Decoder decoder = new StringDecoder(consumerConfig.props());


        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, numThreads);
        final ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, decoder, decoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(topic);
        executor = Executors.newFixedThreadPool(numThreads);

        System.out.printf("KafkaStream Count:%s\n", streams.size());
        // now create an object to consume the messages
        final List<Future<String>> futures = new ArrayList<Future<String>>();
        for (final KafkaStream stream : streams) {
            futures.add(executor.submit(new MessageHandler(stream)));
        }

        //Non-blocking shutdown
        shutdownThread = new Thread() {
            @Override
            public void run() {
                List<String> streamInfo = new ArrayList<String>(futures.size());
                //blocking call
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        streamInfo.add(futures.get(i).get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                for (String info : streamInfo)
                    System.out.println(info);
                //EE clean shutdown
                consumer.shutdown();
                executor.shutdown();
            }
        };
        shutdownThread.start();
    }

    public static class MessageHandler implements Callable<String> {
        private KafkaStream m_stream;
        AtomicInteger streamCount = new AtomicInteger(0);

        public MessageHandler(KafkaStream a_stream) {
            m_stream = a_stream;
        }

        @Override
        public String call() {
            ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
            try {
                while (it.hasNext()) {
                    logger.trace("[Message Consumer] [Thread] " + Thread.currentThread().getName() + ": " + it.next().message());
                    count.incrementAndGet();
                    streamCount.incrementAndGet();
                }
            } catch (ConsumerTimeoutException e) {
                e.printStackTrace();
                System.out.println(" No message is available for consumption after the specified interval: " + Thread.currentThread().getName());
            }
            return String.format("[MessageHandler %s] %d", m_stream.clientId(), streamCount.get());
        }
    }

}
