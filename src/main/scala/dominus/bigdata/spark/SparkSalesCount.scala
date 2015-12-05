package dominus.bigdata.spark

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

/**
 * Compile SparkSalesCount
 * cd /opt/Development/github_repo/archetype-helloworld/target/classes
 * rm SparkSalesCount.jar
 * jar cf SparkSalesCount.jar dominus/bigdata/spark/SparkSalesCount*.class
 * spark-submit --class dominus.bigdata.spark.SparkSalesCount SparkSalesCount.jar
 */
object SparkSalesCount {

  def main(args: Array[String]) {

    val mockData = List("106980|29700|1-JAN-1991|S||29|2581.00|730.80|",
      "229645|3380|1-JAN-1991|S||6| 66.00| 18.00|", "229645|127520|1-JAN-1991|P||3|195.00| 61.20|",
      "4500|67830|1-JAN-1991|C||7|644.00|180.60|", "211950|33030|1-JAN-1991|I||14|1680.00|848.40|",
      "65|179330|1-JAN-1991|T|9403|2|158.00| 82.80|")

    val sc = new SparkContext(new SparkConf().setAppName("Spark Sales Count").setMaster("yarn-cluster"))
    val distFile = sc.textFile("hdfs://scaj31cdh-ns/user/shawguo/data/SalesLog/sales1.dat")
    //    val distFile = sc.parallelize(mockData, 2);
    val outputSuffix: String = new SimpleDateFormat("yyyy_MM_dd_mm").format(new Date)
    println("SalesOutput_" + outputSuffix)

    distFile.map(s => s.split("\\|")).
      map(a => (a(0), Integer.valueOf(a(5)))).
      reduceByKey(_+_).
      repartition(1).
      sortByKey(true).
//      map(a => a.get(0) + "," + a(1)).
    saveAsTextFile("hdfs://scaj31cdh-ns/user/shawguo/data/SalesOutput_" + outputSuffix)


    sc.stop()
  }
}