package dominus.stream.storm.topology.kafka;

import dominus.stream.storm.bolt.DevNullBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

import java.util.UUID;

/**
 * Storm Kafka Integration (0.10.x+)
 * http://storm.apache.org/releases/1.1.1/storm-kafka-client.html
 *
 * NOT COMPATIBLE with STORM 1.0.2
 * java.lang.NoSuchMethodError: org.apache.storm.utils.Time.nanoTime()J at org.apache.storm.kafka.spout.internal.Timer.<init>(Timer.java:45) at org.apache.storm.kafka.spout.KafkaSpout.open(KafkaSpout.j
 */
public class KafkaSpoutTestTopology {
    public static final int DEFAULT_SPOUT_NUM = 1;
    public static final int DEFAULT_BOLT_NUM = 1;
    public static final int DEFAULT_WORKER_NUM = 2;
    public static final int DEFAULT_KAFKA_SPOUT_NUM = 2;

    // names
    public static final String TOPOLOGY_NAME = "Kafka10SpoutTestTopology";
    public static final String SPOUT_ID = "New_Kafka_Spout";
    public static final String BOLT_ID = "Printer_Bolt";

    // default kafka config
    public static final String BOOTSTRAP_SERVERS = "NM-304-HW-XH628V3-BIGDATA-089:9092";
    public static final String KAFKA_TEST_TOPIC = "kafka_bolt_test";

    public static StormTopology getTopology(String bootstrapServers, String... topics) {

        // 1 -  Setup Kafka Spout   --------
        KafkaSpoutConfig<String, String> kafkaConf = KafkaSpoutConfig.
                builder(bootstrapServers, topics)
                .setGroupId(UUID.randomUUID().toString())
                .setProp("enable.auto.commit","true")
                .setProp("auto.offset.reset","earliest")
                .setProp("fetch.min.bytes","2048")
                .setProp("session.timeout.ms","20000")
                .build();
        KafkaSpout spout = new KafkaSpout(kafkaConf);

        // 2 -   Printer Bolt   --------
        DevNullBolt bolt = new DevNullBolt();

        // 3 - Setup Topology  --------
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(SPOUT_ID, spout, DEFAULT_SPOUT_NUM).setNumTasks(DEFAULT_KAFKA_SPOUT_NUM);
        builder.setBolt(BOLT_ID, bolt, DEFAULT_BOLT_NUM)
                .localOrShuffleGrouping(SPOUT_ID);

        return builder.createTopology();
    }

    /**
     * Copies text file content from sourceDir to destinationDir. Moves source files into sourceDir after its done consuming
     */
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.setDebug(true);
        if (args != null && args.length > 0) {
            conf.setNumWorkers(DEFAULT_WORKER_NUM);
            //  Submit to Storm cluster
            StormSubmitter.submitTopologyWithProgressBar(TOPOLOGY_NAME, conf, getTopology(args[0], args[1]));
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME, conf, getTopology(BOOTSTRAP_SERVERS, KAFKA_TEST_TOPIC));
            Utils.sleep(400000);
            cluster.killTopology(TOPOLOGY_NAME);
            cluster.shutdown();
        }
    }
}
