package dominus.bigdata.spark

import java.io.{FileNotFoundException, IOException, FileInputStream}
import java.util.Properties

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

/**
 * User story:
 *
 * Master data work with Big data
 *
 * Oracle Table Access for Hadoop  & Spark (OTA4H)
 * Which of our products[Master data] got a rating of four stars or higher[Big Data], on social media in the last quarter?
 *
 * [Prepare]
 * master data:mysql employees.employees table
 * big data: export mysql employees.salaries to flat file
 * mysqldump -u root -p -t -T/tmp employees --tables salaries --fields-terminated-by='|'
 *
 *
 * [JDBC Driver]
 * spark.driver.extraClassPath  ojdbc6.jar
 * --jars /opt/Development/github_repo/archetype-helloworld/lib/ojdbc/ojdbc6.jar
 * any jars included with the --jars option will be automatically transferred to the cluster(driver & executor).
 *
 *
 */
object SparkJDBCSourceTest {

  val properties = new Properties()
  properties.load(getClass.getClassLoader().getResourceAsStream("jdbc.properties"))
  val jdbcUrl = properties.getProperty("scala.mysql.jdbc.url")
  val dbTable = properties.getProperty("scala.mysql.jdbc.dbtable")
  val username = properties.getProperty("scala.mysql.jdbc.username")
  val password = properties.getProperty("scala.mysql.jdbc.password")

  def main(args: Array[String]) {

    val sc = new SparkContext(new SparkConf().setAppName("Spark JDBC Source Test").setMaster("yarn-cluster"))
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    println(sc.version)

    //mysql master data
    val employeeDF = sqlContext.read.format("jdbc").
      options(
        Map("url" -> jdbcUrl,
          "dbtable" -> dbTable, "user" -> username, "password" -> password)).
      load()

    //big data
    val salaryDF = sc.textFile("hdfs://scaj31cdh-ns/user/shawguo/data/salaries.txt")

    println(employeeDF.count())
    println(salaryDF.count())

    sc.stop()

  }

}
