#!/usr/bin/env bash

base_dir=$(dirname $0)/..

CLASSPATH=$CLASSPATH:$base_dir/target/classes

for file in $base_dir/target/dependency/*.jar
do
  CLASSPATH=$CLASSPATH:$file
done

if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

#echo $CLASSPATH

exec $JAVA $JAVA_OPTS -Dfile.encoding=UTF-8 -cp $CLASSPATH dominus.web.WebApplication "$@"
