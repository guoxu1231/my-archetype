#!/usr/bin/env bash

#Install dubbo dependencies / cached
git clone https://github.com/alibaba/dubbo.git dubbo
cd dubbo
mvn clean install -Dmaven.test.skip -o

#install test_db
git clone https://github.com/datacharmer/test_db.git test_db
cd test_db
mysql < employees.sql
mysql -t < test_employees_md5.sql
#create staging schema
mysql -u root -e "create database IF NOT EXISTS iops_schema;"
mysql -u root -e "show schemas"
cd ..

#install kafka & standalone zookeeper
mkdir $HOME/download -p
wget http://www.us.apache.org/dist/kafka/0.9.0.1/kafka_2.11-0.9.0.1.tgz -O $HOME/download/kafka.tgz
mkdir -p kafka && tar xzf $HOME/download/kafka.tgz -C kafka --strip-components 1
nohup bash -c "cd kafka && bin/zookeeper-server-start.sh config/zookeeper.properties &"
nohup bash -c "cd kafka && bin/kafka-server-start.sh config/server.properties &"

#debug dir info
pwd
ls -l

