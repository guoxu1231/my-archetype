package dominus.bigdata.spark.sql

import java.util.Properties

import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.{SparkConf, SparkContext}
;

/**
 * Writing to JDBC(Oracle), (Number of Partitions:9)
 *
 */
object SparkJDBCWriterTest {

  val SPARK_SALES_RAW_TABLE: String = "spark_sales_raw"

  def main(args: Array[String]) {

    val sc = new SparkContext(new SparkConf().setAppName("Spark JDBC Writer Test").setMaster("yarn-cluster"))
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    val sourceRDD = sc.textFile("hdfs://scaj31cdh-ns/user/shawguo/data/SalesLog/sales1.dat")

    //Programmatically Specifying the Schema for DataFrame
    // The schema is encoded in a string
    val schema =
      StructType(
        StructField("sales_id", StringType) ::
          StructField("sales_count", IntegerType) ::
          StructField("sales_date", StringType) ::
          StructField("sales_code", StringType) ::
          StructField("other_raw_data", StringType) :: Nil)

    // Convert records of the RDD (people) to Rows.
    val rowRDD = sourceRDD.map(_.split("\\|")).map(p => Row(p(0), Integer.valueOf(p(1).trim()), p(2), p(3), p(4)))

    // Apply the schema to the RDD.
    val salesDataDF: DataFrame = sqlContext.createDataFrame(rowRDD, schema)

    //DEBUG
    salesDataDF.printSchema()
    printf("Number of Partitions: %s\n", salesDataDF.rdd.getNumPartitions)
    salesDataDF.sample(false, 0.000001).collect().foreach(println _)
    printf("Number of Rows: %s\n", salesDataDF.count())

    //JDBC Conn
    val conn = JdbcUtils.createConnectionFactory(jdbcUrl, jdbcProps)()
    if (!JdbcUtils.tableExists(conn, jdbcUrl, SPARK_SALES_RAW_TABLE)) {
      conn.prepareStatement(
        "create table scott." + SPARK_SALES_RAW_TABLE + " (sales_id VARCHAR2(25), sales_count INTEGER, sales_date VARCHAR2(25),sales_code VARCHAR2(25), other_raw_data VARCHAR2(100))").executeUpdate()
    }
    assert(JdbcUtils.tableExists(conn, jdbcUrl, SPARK_SALES_RAW_TABLE), "scott." + SPARK_SALES_RAW_TABLE + " does not exists!")

    //save to database in parallel
    JdbcUtils.saveTable(salesDataDF, jdbcUrl, SPARK_SALES_RAW_TABLE, jdbcProps)
  }


  val properties = new Properties()
  properties.load(getClass.getClassLoader().getResourceAsStream("jdbc.properties"))
  val jdbcUrl = properties.getProperty("oracle.jdbc.url")
  //    val dbTable = properties.getProperty("scala.mysql.jdbc.dbtable")
  val username = properties.getProperty("oracle.jdbc.username")
  val password = properties.getProperty("oracle.jdbc.password")
  val driver = properties.getProperty("oracle.jdbc.driverClassName")

  val jdbcProps = new java.util.Properties();
  jdbcProps.put("user", username);
  jdbcProps.put("password", password);
  jdbcProps.put("driver", driver);
}
