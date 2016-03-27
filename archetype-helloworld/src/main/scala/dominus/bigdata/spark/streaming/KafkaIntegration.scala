package dominus.bigdata.spark.streaming

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scala.collection.JavaConverters._

/**
 * Kafka topic(raw data) to Kafka topic
 *
 * Data from:
 * tail -F hdfs-audit.log | bin/kafka-console-producer --broker-list scaj31bda02:9092 --topic hdfs_log_raw
 *
 *
 */
object KafkaIntegration {

  //load from properties
  val properties = new Properties
  properties.load(getClass.getClassLoader().getResourceAsStream("cdh.properties"))
  val props = properties.asScala

  val BATCH_DURATION = Seconds(5)
  //hdfs name node audit log extract by regex
  val cmdRegex =
    """.*cmd=(\w*)\s*.*""".r


  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("Spark Streaming & Kafka Integration Test").setMaster("yarn-cluster")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, BATCH_DURATION)
    //TODO is it supported in spark streaming?
    val accum = sc.accumulator(0, "Kafka RDD Accumulator")

    val kafkaStream = KafkaUtils.createStream(ssc, props("zkQuorum"), "spark-streaming-consumer1", Map {
      "hdfs_log_raw" -> 1
    })

    kafkaStream.map(kafkaMsg => {
      try {
        val cmdRegex(cmd) = kafkaMsg._2
        cmd + "~" + 1
      } catch {
        //NOTE: println to executor stdout
        case ex: scala.MatchError => println("Regex match error:" + kafkaMsg)
        //          (auditLine, 0) todo
      }
    }).foreachRDD(rdd => {
      rdd.foreachPartition { partitionOfRecords =>

        val kafkaProps = new Properties()
        kafkaProps.put("bootstrap.servers", props("bootstrap.servers"))
        kafkaProps.put("client.id", "KafkaIntegration Producer");
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        val producer = new KafkaProducer[String, String](kafkaProps);

        partitionOfRecords.foreach(record => {
          val message = new ProducerRecord[String, String]("hdfs_log_test", record.asInstanceOf[String])
          producer.send(message)
          //TODO is it supported in spark streaming?
          accum.add(1)
        })
        producer.close()

       // printf("Kafka RDD Accumulator: %s\n", accum)
//        printf("PartionOfRecords: %s\n", partitionOfRecords.length)
      }
    })


    ssc.start()
    ssc.awaitTermination()
  }


}
