package dominus.stream.storm.topology.kafka;

import dominus.stream.storm.spout.RandomSentenceSpout;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

import java.util.Properties;

/**
 * storm/examples/storm-kafka-examples/
 * https://github.com/apache/storm/blob/v1.1.1/examples/storm-kafka-examples/src/main/java/org/apache/storm/kafka/trident/KafkaProducerTopology.java
 */
public class KafkaBoltTestTopology {
    /**
     * @param brokerUrl Kafka broker URL
     * @param topicName Topic to which publish sentences
     * @return A Storm topology that produces random sentences using {@link RandomSentenceSpout} and uses a {@link KafkaBolt} to
     * publish the sentences to the kafka topic specified
     */
    public static StormTopology newTopology(String brokerUrl, String topicName) {
        final TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("Random_Sentence_Spout", new RandomSentenceSpout.TimeStamped(""), 4).setNumTasks(200);

        /* The output field of the RandomSentenceSpout ("word") is provided as the boltMessageField
          so that this gets written out as the message in the kafka topic. */
        final KafkaBolt<String, String> bolt = new KafkaBolt<String, String>()
                .withProducerProperties(newProps(brokerUrl, topicName))
                .withTopicSelector(new DefaultTopicSelector(topicName))
                .<String, String>withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("key", "word"));

        builder.setBolt("Kafka_Bolt", bolt, 2).shuffleGrouping("Random_Sentence_Spout").setNumTasks(2);

        return builder.createTopology();
    }

    /**
     * @return the Storm config for the topology that publishes sentences to kafka using a kafka bolt.
     */
    private static Properties newProps(final String brokerUrl, final String topicName) {
        return new Properties() {{
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            put(ProducerConfig.CLIENT_ID_CONFIG, topicName);
        }};
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.setDebug(true);

        if (args != null && args.length > 0) {
            conf.setNumWorkers(2);
            StormSubmitter.submitTopologyWithProgressBar("KafkaBoltTestTopology", conf, KafkaBoltTestTopology.newTopology(args[0], args[1]));
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("KafkaBoltTestTopology", conf, KafkaBoltTestTopology.newTopology("NM-304-HW-XH628V3-BIGDATA-089:9092", "kafka_bolt_test"));
            Utils.sleep(400000);
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }
}
