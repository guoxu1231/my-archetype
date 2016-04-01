package dominus.bigdata.spark.apis

import java.net.URLClassLoader
import java.util.Properties

import org.apache.spark.{SparkConf, SparkContext}


/**
 * Test JDBC Source & Driver connectivity
 * Since spark 1.5.2
 */
//NOTE: JDBC Driver must be in classpth of bootstrap classloader, configed by spark.driver.extraClassPath or other approach
/**
 * The JDBC driver class must be visible to the primordial class loader on the client session and on all executors.
 * This is because Java’s DriverManager class does a security check that results in it ignoring all drivers not visible to the primordial class loader when one goes to open a connection.
 * One convenient way to do this is to modify compute_classpath.sh on all worker nodes to include your driver JARs.
 */
object SparkJDBCDriverTest {
  def main(args: Array[String]) {

    val properties = new Properties()
    properties.load(getClass.getClassLoader().getResourceAsStream("jdbc.properties"))
    val jdbcUrl = properties.getProperty("oracle.jdbc.url")
    val username = properties.getProperty("oracle.jdbc.username")
    val password = properties.getProperty("oracle.jdbc.password")

    val sc = new SparkContext(new SparkConf().setAppName("Spark JDBC Driver Test").setMaster("yarn-cluster"))
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    println(sc.version)

    //print class loader
    /**
     * [Yarn-cluster]
     * [ClassLoader]org.apache.spark.util.MutableURLClassLoader@10c8f62
       [ClassLoader]sun.misc.Launcher$AppClassLoader@4e25154f
     */
    var current = Thread.currentThread().getContextClassLoader
    while (current.getParent != null) {
      println("[ClassLoader]" + current.toString)
      current = current.getParent
    }
    //print class path
    /**
     * [org.apache.spark.util.MutableURLClassLoader@25f7391e] classpath
     * app jar & --jars
        file:__app__.jar
        file:ojdbc6.jar
     */
    printf("[Classpath_%s] \n", Thread.currentThread().getContextClassLoader)
    Thread.currentThread().getContextClassLoader().asInstanceOf[URLClassLoader].getURLs().foreach(println _)


    //    oracle connection test
    val employeeDF = sqlContext.read.format("jdbc").
      options(
        Map("url" -> jdbcUrl,
          "dbtable" -> "ACTIVEMQ_MSGS", "user" -> username, "password" -> password)).
      load()

    println(employeeDF.count())

    sc.stop()

  }
}
