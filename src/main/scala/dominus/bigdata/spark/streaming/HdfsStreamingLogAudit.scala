package dominus.bigdata.spark.streaming

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{DStream, PairDStreamFunctions}


import scala.util.matching.Regex

/**
 * NameNode :tail -f hdfs-audit.log | nc -lk 9999
 * -k Forces nc to stay listening for another connection after its current connection is completed.
 */
object HdfsStreamingLogAudit {

  val BATCH_DURATION = Seconds(10)
  val WINDOW_DURATION = BATCH_DURATION * 6

  def main(args: Array[String]) {

    if (args.length < 2) {
      System.err.println("Usage: HdfsStreamingLogAudit <hostname> <port>")
      System.exit(1)
    }

    Logger.getRootLogger.setLevel(Level.WARN)


    val sparkConf = new SparkConf().setAppName("HDFS StreamingLog Audit").setMaster("yarn-cluster")
    val ssc = new StreamingContext(sparkConf, BATCH_DURATION)
    //NOTE: Checkpointing must be enabled for applications with usage of stateful transformations
    ssc.checkpoint("hdfs:///user/shawguo/spark-checkpointing")
    val hdfsAuditLog = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)

    //demo 1
    //cmdCount(ssc, hdfsAuditLog)

    //demo 2
    cmdStat(ssc, hdfsAuditLog)


    ssc.start()
    println("Hello HDFS StreamingLog Audit")
    //    ssc.awaitTerminationOrTimeout(60000 * 5)
    ssc.awaitTermination()
  }


  def todo_1(ssc: StreamingContext, hdfsAuditLog: DStream[String]) {
    val cmdDetailRegex =
      """.*cmd=(\w*)\s*src=(\w*)\s*.*""".r
    val cmdRegex =
      """.*cmd=(\w*)\s*.*""".r

    val initialCreatedFileRdd = ssc.sparkContext.parallelize(List(("src file", List())))

    val createdRdd = hdfsAuditLog.map(auditLine => {
      try {
        val cmdRegex(cmd, src) = auditLine
        println((src, cmd)) //debug info in the executor
        (src, cmd)
      } catch {
        //NOTE: println to executor stdout
        case ex: scala.MatchError => {
        println("Regex match error:" + auditLine)
        "hello_world"
          }
      }
    }).asInstanceOf[DStream[(String, String)]]
      .groupByKey()
//      .groupByKeyAndWindow(Seconds(120), Seconds(60))
      .filter(cmdDetails => {
          val cmdList: List[String] = cmdDetails._2.toList
          cmdList.contains("create") && !cmdList.contains("delete") //new files and not be deleted
      })

    //    val stateCmdCount = cmdCount.mapWithState(StateSpec.function(mappingFunc).initialState(initialRDD))
    //      .window(WINDOW_DURATION).mapWithState(
    //      StateSpec.function(mappingFunc).initialState(initialRDD))
    //    val stateDstream = wordDstream


    /**
    cmdCount.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        //        println(rdd)
        //        rdd.collect().foreach(createCmd => println(createCmd))
        rdd.distinct().foreach(println _)
      }
      //print CST timestamp
      val sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
      sdf.setTimeZone(TimeZone.getTimeZone("CST"))
      println("[DStream]" + sdf.format(new Date()))
    })
      * */


  }


  /**
   * Count hdfs cmd in current batch interval
   * [DStream]2015-06-20 08:06:44
     (listStatus,256)
     (getfileinfo,9)
     (rename,3)
     (create,3)
     (open,1)
     (delete,2)
   * @param ssc
   * @param hdfsAuditLog
   */
  def cmdCount(ssc: StreamingContext, hdfsAuditLog: DStream[String]) {
    val cmdRegex = """.*cmd=(\w*)\s*.*""".r

    val createdRdd = hdfsAuditLog.map(auditLine => {
      try {
        val cmdRegex(cmd) = auditLine
        (cmd, 1)
      } catch {
        //NOTE: println to executor stdout
        case ex: scala.MatchError => {
          println("Regex match error:" + auditLine)
          ("scala.MatchError", 1) //TODO error handling
        }
      }
    })
      .reduceByKey(_ + _)
    //      .groupByKeyAndWindow(Seconds(120), Seconds(60))

    createdRdd.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        rdd.collect().foreach(println _)
      }
      //print CST timestamp
      val sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
      sdf.setTimeZone(TimeZone.getTimeZone("CST"))
      println("[DStream]" + sdf.format(new Date()))
    })

  }


  /**
   *
   * Statistic hdfs cmd in hdfs audit log (stateful)
   * Streaming State management, mapWithState
   * -------------------------------------------
     Time: 1450618050000 ms
     -------------------------------------------
    (setTimes,35)
    (setReplication,2)
    (mkdirs,348)
    (listCacheDirectives,275)
    (listStatus,490564)
    (getfileinfo,27710)
    (rename,244)
    (create,385)
    (open,5105)
    (placeholder,0)
    (setPermission,5)
    (listCachePools,558)
    (contentSummary,74976)
    (delete,403)
   * @param ssc
   * @param hdfsAuditLog
   */
  def cmdStat(ssc: StreamingContext, hdfsAuditLog: DStream[String]) {


    val cmdRegex = """.*cmd=(\w*)\s*.*""".r
    val initialCmdStatRdd = ssc.sparkContext.parallelize(List(("TODO", 0))) //TODO default state

    // Update the cumulative count using mapWithState
    // This will give a DStream made of state (which is the cumulative count of the hdfsCmds)
    val mappingFunc = (hdfsCmd: String, one: Option[Int], state: State[Int]) => {
      val sum = one.getOrElse(0) + state.getOption.getOrElse(0)
      val output = (hdfsCmd, sum)
      state.update(sum)
      output
    }
    val cmdStatSnapshot = hdfsAuditLog.map(auditLine => {
      try {
        val cmdRegex(cmd) = auditLine
        (cmd, 1)
      } catch {
        //NOTE: println to executor stdout
        case ex: scala.MatchError => println("Regex match error:" + auditLine)
        //          (auditLine, 0) todo
      }
    }).asInstanceOf[DStream[(String, Int)]].
      reduceByKey(_ + _).
      mapWithState(StateSpec.function(mappingFunc).initialState(initialCmdStatRdd)).stateSnapshots()
    //NOTE: maintain perkey state and manage that state using an updateFunction

    cmdStatSnapshot.print(100)
  }


}
