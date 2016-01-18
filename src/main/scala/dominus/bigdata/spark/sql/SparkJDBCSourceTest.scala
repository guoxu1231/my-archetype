package dominus.bigdata.spark.sql

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, Properties}

import org.apache.spark.{SparkConf, SparkContext}

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


    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")

    //emp salary report
    salaryDF.map(line => line.split("\\|")).
      // get effective salary
      filter(row => {
        try {
          val fromDate = format.parse(row(2))
          val toDate = format.parse(row(3))
          val today = new Date()
          if (today.getTime() > fromDate.getTime() && today.getTime() < toDate.getTime())
            true
          else
            false
        } catch {
          case e: ParseException => false
        }
      }).
      // join with master data
      keyBy(row => Integer.valueOf(row(0))).join(employeeDF.rdd.keyBy(row => row(0).asInstanceOf[Integer])).
      // format output
      mapValues(row => row._2(2) + "," + row._2(3) + "    " + row._1(1) + "  [from_date] " + row._1(2) + "  [to_date] " + row._1(3))
      .values.repartition(1).
      saveAsTextFile("hdfs://scaj31cdh-ns/user/shawguo/data/emp_salary_report_"
        + new SimpleDateFormat("yyyy_MM_dd_mm").format(new Date))

    /**
     * Sample output
     * Georgi,Facello    88958  [from_date] 2002-06-22  [to_date] 9999-01-01
     * Parto,Bamford    43311  [from_date] 2001-12-01  [to_date] 9999-01-01
     */

    //    println(employeeDF.count())
    //    println(salaryDF.count())

    sc.stop()

  }

}
