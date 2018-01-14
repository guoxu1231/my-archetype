//package dominus.stream.storm.topology.kafka;
//
//import dominus.stream.storm.bolt.DevNullBolt;
//import org.apache.storm.Config;
//import org.apache.storm.LocalCluster;
//import org.apache.storm.StormSubmitter;
//import org.apache.storm.generated.StormTopology;
//import org.apache.storm.topology.TopologyBuilder;
//import org.apache.storm.utils.Utils;
//
//import java.util.UUID;
//
///**
// * Storm Kafka Integration (0.8.x)
// * http://storm.apache.org/releases/1.1.1/storm-kafka.html
// */
//@Deprecated
//public class Kafka08SpoutTestTopology {
//
//    public static final int DEFAULT_SPOUT_NUM = 1;
//    public static final int DEFAULT_BOLT_NUM = 1;
//    public static final int DEFAULT_WORKER_NUM = 2;
//
//    // names
//    public static final String TOPOLOGY_NAME = "Kafka08SpoutTestTopology";
//    public static final String SPOUT_ID = "Kafka_Spout";
//    public static final String BOLT_ID = "Printer_Bolt";
//
//    public static StormTopology getTopology(String zkConnString, String topicName) {
//
//        // 1 -  Setup Kafka Spout   --------
//        BrokerHosts brokerHosts = new ZkHosts(zkConnString);
//        SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, topicName, "/" + topicName, UUID.randomUUID().toString());
//        spoutConfig.scheme = new StringMultiSchemeWithTopic();
//        spoutConfig.ignoreZkOffsets = true;
//        KafkaSpout spout = new KafkaSpout(spoutConfig);
//
//        // 2 -   Printer Bolt   --------
//        DevNullBolt bolt = new DevNullBolt();
//
//        // 3 - Setup Topology  --------
//        TopologyBuilder builder = new TopologyBuilder();
//        builder.setSpout(SPOUT_ID, spout, DEFAULT_SPOUT_NUM).setNumTasks(10);
//        builder.setBolt(BOLT_ID, bolt, DEFAULT_BOLT_NUM)
//                .localOrShuffleGrouping(SPOUT_ID);
//
//        return builder.createTopology();
//    }
//
//    /**
//     * Copies text file content from sourceDir to destinationDir. Moves source files into sourceDir after its done consuming
//     */
//    public static void main(String[] args) throws Exception {
//        Config conf = new Config();
//        conf.setDebug(true);
//        if (args != null && args.length > 0) {
//            conf.setNumWorkers(DEFAULT_WORKER_NUM);
//            //  Submit to Storm cluster
//            StormSubmitter.submitTopologyWithProgressBar(TOPOLOGY_NAME, conf, getTopology(args[0], args[1]));
//        } else {
//            LocalCluster cluster = new LocalCluster();
//            cluster.submitTopology(TOPOLOGY_NAME, conf, getTopology("TEST-BDD-064:2181/kafka", "kafka_bolt_test"));
//            Utils.sleep(400000);
//            cluster.killTopology(TOPOLOGY_NAME);
//            cluster.shutdown();
//        }
//    }
//
//
//}
