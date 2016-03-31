#!/usr/bin/env bash

#Install dubbo dependencies / cached
#git clone https://github.com/alibaba/dubbo.git dubbo
#cd dubbo
#mvn clean install -Dmaven.test.skip -o

git clone https://github.com/datacharmer/test_db.git test_db
cd test_db
mysql < employees.sql
mysql -t < test_employees_md5.sql
mysql -u root -e "show schemas"