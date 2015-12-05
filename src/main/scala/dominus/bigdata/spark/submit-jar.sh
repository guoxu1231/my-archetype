#!/usr/bin/env bash

cd /opt/Development/github_repo/archetype-helloworld/target/classes
rm SparkSalesCount.jar
jar cf SparkSalesCount.jar dominus/bigdata/spark/SparkSalesCount*.class
spark-submit --class dominus.bigdata.spark.SparkSalesCount SparkSalesCount.jar