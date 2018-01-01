#!/usr/bin/env bash

export FLINK_HOME=/opt/Development/middleware/bigdata/flink-1.4.0
export JAR_HOME=/opt/Development/github_repo/my-archetype/archetype-hello-stream-flink/target


$FLINK_HOME/bin/flink run --class jar.stream.Kafka010Example --parallelism 4 --jobmanager nm-304-hw-xh628v3-bigdata-096:30419 \
 $JAR_HOME/archetype-hello-stream-flink-1.0-SNAPSHOT.jar \
--input-topic eventsim --output-topic flink_kafka_sink \
--bootstrap.servers NM-304-HW-XH628V3-BIGDATA-089:9092 --zookeeper.connect TEST-BDD-062:2181,TEST-BDD-063:2181,TEST-BDD-064:2181/kafka \
--group.id flink_kafka_sink_01011 --prefix flink
--auto.offset.reset earliest