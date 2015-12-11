#!/usr/bin/env bash

cd /opt/Development/github_repo/archetype-helloworld/target/classes
rm --verbose my-spark-test.jar
jar vcf my-spark-test.jar dominus/bigdata/spark/*.class jdbc.properties

#[MySql JDBC]
#spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/maven_repo/mysql/mysql-connector-java/5.1.37/mysql-connector-java-5.1.37.jar my-spark-test.jar
#[Oracle JDBC]
#spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/github_repo/archetype-helloworld/lib/ojdbc/ojdbc6.jar my-spark-test.jar
#spark-submit --class dominus.bigdata.spark.SparkSalesCount my-spark-test.jar



#[Streaming Example]
cd /opt/Development/github_repo/archetype-helloworld/target/classes
rm --verbose my-spark-stream-test.jar
jar vcf my-spark-stream-test.jar dominus/bigdata/spark/streaming/example/*.class log4j.properties

spark-submit --class dominus.bigdata.spark.streaming.example.NetworkWordCount my-spark-stream-test.jar slc07pbj.us.oracle.com 9999