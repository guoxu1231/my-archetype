package dominus.intg.jms.kafka;


import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.utils.VerifiableProperties;
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
 * Kafka 0.8.2.0
 */
public class KafkaFastProducer {

    public static void main(String[] args) {
        long events = 1000L;
        Random rnd = new Random();

        Properties props = new Properties();
        //EE "host_name:9092"
        props.put("metadata.broker.list", args[0]);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "dominus.intg.jms.kafka.KafkaProducer$SimplePartitioner");
        //EE: This option provides the lowest latency but the weakest durability guarantees (some data will be lost when a server fails).
        props.put("request.required.acks", "0");
        props.put("request.timeout.ms", "600000"); //for kafka server debug purpose
        //EE: By setting the producer to async we allow batching together of requests (which is great for throughput) but open the possibility of a failure of the client machine dropping unsent data.
        props.put("producer.type", "async");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);

        StopWatch watch = new StopWatch("[KafkaFastProducer] events:" + events);
        watch.start();
        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;
            KeyedMessage<String, String> data = new KeyedMessage<String, String>("page_visits", ip, msg);
            System.out.println("[Message Producer]:" + data);
            producer.send(data);
        }
        producer.close();
        watch.stop();
        System.out.println(watch);
    }
}



