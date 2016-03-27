#!/usr/bin/env bash

export SRC_HOME=/opt/Development/github_repo/my-archetype/archetype-helloworld/src/main/java

$JAVA_HOME/bin/xjc -p dominus.intg.ws.jaxb.javabean -d $SRC_HOME Person.xsd -verbose -mark-generated