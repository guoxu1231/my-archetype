#!/usr/bin/env bash

#EE:[disabled] Install dubbo dependencies / cached
#git clone https://github.com/alibaba/dubbo.git dubbo
#cd dubbo
#mvn clean install -Dmaven.test.skip -o

#install test_db
git clone https://github.com/datacharmer/test_db.git test_db
cd test_db
mysql < employees.sql
mysql -t < test_employees_md5.sql
#create staging schema
mysql -u root -e "create database IF NOT EXISTS iops_schema;"
mysql -u root -e "show schemas"
#enable master replication
sudo /etc/init.d/mysql stop || true
echo '[mysqld]'            | sudo tee /etc/mysql/conf.d/replication.cnf
echo 'log-bin=mysql-bin'   | sudo tee -a /etc/mysql/conf.d/replication.cnf
echo 'server-id=1'         | sudo tee -a /etc/mysql/conf.d/replication.cnf
echo 'binlog-format = row' | sudo tee -a /etc/mysql/conf.d/replication.cnf
sudo /etc/init.d/mysql start || true
#sudo tail -1000 /var/log/syslog
cd ..

#install kafka & standalone zookeeper
mkdir $HOME/download -p
wget http://www-us.apache.org/dist/kafka/0.10.2.1/kafka_2.11-0.10.2.1.tgz -O $HOME/download/kafka.tgz
mkdir -p kafka && tar xzf $HOME/download/kafka.tgz -C kafka --strip-components 1
#export JMX_PORT=9991
nohup bash -c "cd kafka && bin/zookeeper-server-start.sh config/zookeeper.properties &"
nohup bash -c "cd kafka && bin/kafka-server-start.sh config/server.properties &"

#install elasticsearch
cd $HOME/download
curl -O https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/tar/elasticsearch/2.3.4/elasticsearch-2.3.4.tar.gz
tar zxvf elasticsearch-2.3.4.tar.gz
nohup bash -c "elasticsearch-2.3.4/bin/elasticsearch &"

#start micro-service project
#nohup bash -c "mvn -pl archetype-microservice compile spring-boot:run &"

#debug dir info
pwd
ls -l

