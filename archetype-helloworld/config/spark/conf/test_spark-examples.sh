#!/usr/bin/env bash

:'
[Required]
    spark-defaults.conf
        must be yarn-cluster, yarn-client seems not work

    hdfs://scaj31cdh-ns/user/shawguo/spark-1.5.2-jars
    -rw-r--r--   3 shawguo hadoop  183993445 2015-12-04 13:40 /user/shawguo/spark-1.5.2-jars/spark-assembly-1.5.2-hadoop2.6.0.jar
    -rw-r--r--   3 shawguo hadoop  118360126 2015-12-04 13:48 /user/shawguo/spark-1.5.2-jars/spark-examples-1.5.2-hadoop2.6.0.jar
'
./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    hdfs://scaj31cdh-ns/user/shawguo/spark-1.5.2-jars/spark-examples-1.5.2-hadoop2.6.0.jar \
    10



