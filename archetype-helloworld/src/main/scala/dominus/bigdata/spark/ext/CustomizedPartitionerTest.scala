package dominus.bigdata.spark.ext

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, Partitioner}


object CustomizedPartitionerTest {


  def main(args: Array[String]) {

    val data = Array(('b', 3), ('c', 1), ('a', 1), ('b', 2), ('b', 1), ('a', 2))


    val conf = new SparkConf().setAppName("CustomizedPartitionerTest").setMaster("local[2]")
    val sc = new SparkContext(conf)

    /**
     * Custom Partitioners can be used only with RDD to type Key/ Value 
     * i.e. PairRDDFunctions.partitionBy(partitioner: Partitioner)
     */
    val distData = sc.parallelize(data, 1).map(u => (u._1, u._2)).partitionBy(new AlphbetPartitioner).map(u => u._1 + "," + u._2 + "\t")
    print(distData.asInstanceOf[RDD[String]].getNumPartitions)
    distData.saveAsTextFile("/tmp/alphbet_partitioner")
  }


}

class AlphbetPartitioner extends Partitioner {

  override def numPartitions: Int = 26

  override def getPartition(key: Any): Int = {

    return key.asInstanceOf[scala.Char].asDigit % 26
  }
}

class CustmozedPartitioner extends Partitioner {

  override def numPartitions: Int = 4

  override def getPartition(key: Any): Int = {
    key match {
      case "data_set_1" => 0
      case "data_set_2" => 1
      case "data_set_3" => 2
      case _ => 4
    }
  }
}