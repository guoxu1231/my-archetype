package dominus.bigdata.spark

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

    val sc = new SparkContext(new SparkConf().setAppName("Spark Sales Count").setMaster("yarn-cluster"))
    val distFile = sc.textFile("hdfs://scaj31cdh-ns/user/shawguo/data/SalesLog/sales1.dat")
    println(distFile.count())
    sc.stop()
  }
}