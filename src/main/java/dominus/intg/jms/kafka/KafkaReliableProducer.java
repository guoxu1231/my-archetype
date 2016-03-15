package dominus.intg.jms.kafka;


import dominus.PropertiesLoader;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Kafka 0.8.2.2 Legacy Scala producer API
 */
@Deprecated
public class KafkaReliableProducer {

    public static void main(String... args) {

        Random rnd = new Random();


        Properties cdhProps = PropertiesLoader.loadCDHProperties();
        Properties props = new Properties();
        //EE "host_name:9092"
        props.put("metadata.broker.list", cdhProps.getProperty("kafka.metadata.broker.list"));
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "dominus.intg.jms.kafka.KafkaProducer$SimplePartitioner");
        long events = Long.valueOf(cdhProps.getProperty("kafka.test.topic.msgCount"));

        //EE: -1, which means that the producer gets an acknowledgement after all in-sync replicas have received the data.
        //EE This option provides the best durability, we guarantee that no messages will be lost as long as at least one in sync replica remains.
        props.put("request.required.acks", "-1");
        props.put("producer.type", "sync");

        //DEBUG
        props.put("request.timeout.ms", "600000"); //for kafka server debug purpose


        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);

        StopWatch watch = new StopWatch("[KafkaReliableProducer] events:" + events);
        watch.start();
        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;
            KeyedMessage<String, String> data = new KeyedMessage<String, String>(args[0], ip, msg);
            System.out.println("[Message Producer]:" + data);
            producer.send(data);
        }
        producer.close();
        watch.stop();
        System.out.println(watch);
    }
}



