#!/usr/bin/env bash


PATH=$PATH:/opt/Development/middleware/bigdata/spark-1.6.0-SNAPSHOT-bin-hadoop2.6/bin
export SPARK_HOME=/opt/Development/middleware/bigdata/spark-1.6.0-SNAPSHOT-bin-hadoop2.6
#TODO test socketstream bug
#PATH=$PATH:/opt/Development/github_repo/spark/dist/bin
#export SPARK_HOME=/opt/Development/github_repo/spark/dist

#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n

cd /opt/Development/github_repo/my-archetype/archetype-helloworld/target/classes



#[SparkSQL]
rm --verbose my-spark-test.jar
jar vcf my-spark-test.jar dominus/bigdata/spark/sql/*.class jdbc.properties

#[MySql JDBC]
#spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/maven_repo/mysql/mysql-connector-java/5.1.37/mysql-connector-java-5.1.37.jar my-spark-test.jar
spark-submit --class dominus.bigdata.spark.sql.BroadcastJoinTest --jars /opt/Development/maven_repo/mysql/mysql-connector-java/5.1.37/mysql-connector-java-5.1.37.jar my-spark-test.jar
#[Oracle JDBC]
#spark-submit --class dominus.bigdata.spark.sql.SparkJDBCWriterTest --jars /opt/Development/github_repo/my-archetype/archetype-helloworld/lib/ojdbc/ojdbc6.jar my-spark-test.jar
#spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/github_repo/archetype-helloworld/lib/ojdbc/ojdbc6.jar my-spark-test.jar
#${SPARK_HOME}/bin/spark-submit --class dominus.bigdata.spark.SparkSalesCount --jars /opt/Development/github_repo/archetype-helloworld/target/classes/test.jar my-spark-test.jar
#spark-submit --class dominus.bigdata.spark.SparkApiTest my-spark-test.jar


#[Streaming Example]
#rm --verbose my-spark-stream-test.jar
#jar vcf my-spark-stream-test.jar dominus/bigdata/spark/streaming/example/*.class log4j.properties
#jar vcf my-spark-stream-test.jar dominus/bigdata/spark/streaming/*.class cdh.properties
#spark-submit --class dominus.bigdata.spark.streaming.KafkaIntegration --jars hdfs://scaj31cdh-ns/user/shawguo/spark-1.5.2-jars/spark-streaming-kafka-assembly_2.10-1.6.0.jar,hdfs://scaj31cdh-ns/user/shawguo/spark-1.5.2-jars/kafka-clients-0.8.2.0.jar my-spark-stream-test.jar
#spark-submit --class dominus.bigdata.spark.streaming.example.NetworkWordCount my-spark-stream-test.jar slc07pbj.us.oracle.com 9999
#spark-submit --class dominus.bigdata.spark.streaming.HdfsStreamingLogAudit my-spark-stream-test.jar scaj31bda02.us.oracle.com 9999
#spark-submit --class dominus.bigdata.spark.streaming.example.StatefulNetworkWordCount my-spark-stream-test.jar slc07pbj.us.oracle.com 9999


#[Spark API Test]
#rm --verbose api_test.jar
#jar vcf api_test.jar dominus/bigdata/spark/apis/*.class jdbc.properties
#${SPARK_HOME}/bin/spark-submit --class dominus.bigdata.spark.apis.SparkJDBCDriverTest --jars /opt/Development/github_repo/archetype-helloworld/lib/ojdbc/ojdbc6.jar api_test.jar