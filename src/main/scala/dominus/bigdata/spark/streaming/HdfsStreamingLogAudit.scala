package dominus.bigdata.spark.streaming

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
 * NameNode :tail -f hdfs-audit.log | nc -lk 9999
 * -k Forces nc to stay listening for another connection after its current connection is completed.
 */
object HdfsStreamingLogAudit {

  def main(args: Array[String]) {

    if (args.length < 2) {
      System.err.println("Usage: HdfsStreamingLogAudit <hostname> <port>")
      System.exit(1)
    }

    Logger.getRootLogger.setLevel(Level.WARN)


    val sparkConf = new SparkConf().setAppName("HDFS StreamingLog Audit").setMaster("yarn-cluster")
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    val hdfsAuditLog = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)

    val createCmdLines = hdfsAuditLog.filter(row => row.contains("cmd=listStatus"))
    hdfsAuditLog.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        //        println(rdd)
        rdd.collect().foreach(createCmd => println(createCmd))
      }
    })

    ssc.start()
    println("Hello HDFS StreamingLog Audit")
    //    ssc.awaitTerminationOrTimeout(60000 * 60)
    ssc.awaitTermination()
  }
}
