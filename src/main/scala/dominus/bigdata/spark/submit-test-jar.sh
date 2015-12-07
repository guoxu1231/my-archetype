#!/usr/bin/env bash

cd /opt/Development/github_repo/archetype-helloworld/target/classes
rm --verbose my-spark-test.jar
jar vcf my-spark-test.jar dominus/bigdata/spark/*.class jdbc.properties

#[MySql JDBC]
spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/maven_repo/mysql/mysql-connector-java/5.1.37/mysql-connector-java-5.1.37.jar my-spark-test.jar
#[Oracle JDBC]
#spark-submit --class dominus.bigdata.spark.SparkJDBCSourceTest --jars /opt/Development/github_repo/archetype-helloworld/lib/ojdbc/ojdbc6.jar my-spark-test.jar
#spark-submit --class dominus.bigdata.spark.SparkSalesCount my-spark-test.jar


