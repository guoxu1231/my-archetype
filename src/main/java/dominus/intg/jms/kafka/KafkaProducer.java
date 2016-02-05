package dominus.intg.jms.kafka;


import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.utils.VerifiableProperties;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * from kafka 0.8.0 Producer Example
 * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
 * <p/>
 * Prerequisite:
 * bin/zookeeper-server-start.sh config/zookeeper.properties
 * bin/kafka-server-start.sh config/server.properties
 * bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic page_visits
 * bin/kafka-topics.sh --create --topic page_visits --replication-factor 3 --zookeeper zk_host:2181 --partition 5
 * <p/>
 * Check data:
 * bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic page_visits --from-beginning
 *
 * Kafka 0.8.2.0
 *
 */
public class KafkaProducer {

    public static void main(String[] args) {
        long events = 1000L;
        Random rnd = new Random();

        Properties props = new Properties();
        //EE "host_name:9092"
        /**
         * This does not need to be the full set of Brokers in your cluster but should include at least two in case the first Broker is not available.
         * No need to worry about figuring out which Broker is the leader for the topic (and partition),
         * the Producer knows how to connect to the Broker and ask for the meta data then connect to the correct Broker.
         */
        props.put("metadata.broker.list", args[0]);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "dominus.intg.jms.kafka.KafkaProducer$SimplePartitioner");
        //EE: Without this setting the Producer will 'fire and forget' possibly leading to data loss.

        props.put("request.required.acks", "1");
        props.put("request.timeout.ms", "600000"); //for kafka server debug purpose

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<String, String>(config);

        for (long nEvents = 0; nEvents < events; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;
            KeyedMessage<String, String> data = new KeyedMessage<String, String>("page_visits", ip, msg);
            System.out.println("[Message Producer]:" + data);
            producer.send(data);
        }
        producer.close();
    }


    @SuppressWarnings("UnusedDeclaration")
    public static class SimplePartitioner implements Partitioner {
        public SimplePartitioner(VerifiableProperties properties) {
        }

        public int partition(Object key, int numberOfPartitions) {
            int partition = 0;
            String stringKey = (String) key;
            int offset = stringKey.lastIndexOf('.');
            if (offset > 0) {
                partition = Integer.parseInt(stringKey.substring(offset + 1)) % numberOfPartitions;
            }
            return partition;
        }
    }

}



