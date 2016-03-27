package dominus.intg.jms.kafka.producer;


import dominus.PropertiesLoader;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Send message in batch(async) model, less latency time but possibly lost message
 *
 * StopWatch '[KafkaFastProducer] events:1000': running time (millis) = 1942; [] took 1942 = 100%
 * StopWatch '[KafkaReliableProducer] events:1000': running time (millis) = 214664; [] took 214664 = 100%
 *
 * Kafka 0.8.2.2 Legacy Scala producer API
 */
@Deprecated
public class KafkaFastProducer {
    static final Logger logger = LoggerFactory.getLogger(KafkaFastProducer.class);

    public static void main(String... args) {
        Random rnd = new Random();

        Properties props = new Properties();
        //EE "host_name:9092"
        props.put("metadata.broker.list", args[2]);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "dominus.intg.jms.kafka.ext.RoundRobinPartitioner");
        //EE: This option provides the lowest latency but the weakest durability guarantees (some data will be lost when a server fails).
        props.put("request.required.acks", "0");
        props.put("request.timeout.ms", "600000"); //for kafka server debug purpose
        //EE: By setting the producer to async we allow batching together of requests (which is great for throughput) but open the possibility of a failure of the client machine dropping unsent data.
        props.put("producer.type", "async");
        long events = Long.valueOf(args[1]);

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);

        StopWatch watch = new StopWatch("[KafkaFastProducer] events:" + events);
        watch.start();
        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;
            KeyedMessage<String, String> data = new KeyedMessage<String, String>(args[0], ip, msg);
            logger.trace("[Message Producer]:" + data);
            producer.send(data);
        }
        producer.close();
        watch.stop();
        System.out.println(watch);
    }
}



