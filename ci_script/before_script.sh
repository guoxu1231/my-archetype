#!/usr/bin/env bash

#Install dubbo dependencies
git clone https://github.com/alibaba/dubbo.git dubbo
cd dubbo
mvn clean install -Dmaven.test.skip -o