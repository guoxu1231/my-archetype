package dominus.bigdata.spark.apis

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.Queue

/**
 * Simple DStream API Test
 *
 * Spark 1.6 2016/02/16
 */
object DStreamTransformApiTest {

  def main(args: Array[String]) {

    val batchDuration = Seconds(3)

    //--------------------------------------------------------------test data prepare
    val t1 = 1 to 100 by 3
    val t2 = 1 to 100 by 4
    val t3 = 1 to 100 by 5

    val t6 = List(2, 2, 4, 5, 6)
    val t4 = List(4, 4, 5, 6, 4)
    val t5 = List(1, 3, 4, 5, 6)
    //------------------------------------------------------------------------------

    val testData = new Queue[RDD[Int]]


    val sc = new SparkContext(new SparkConf().setAppName("Spark RDD API Test").setMaster("local[2]"))
    testData += sc.parallelize(t6, 2);
    testData += sc.parallelize(t4, 2);
    testData += sc.parallelize(t5, 2);


    val ssc = new StreamingContext(sc, Seconds(3))
    //EE: window and transform
    val testDstream = ssc.queueStream(testData).window(Seconds(9), Seconds(9)).transform((x: RDD[Int]) => x.distinct())


    testDstream.print(100)


    ssc.start()
    ssc.awaitTerminationOrTimeout(60000)
  }

}
