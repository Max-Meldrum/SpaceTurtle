#! /bin/sh

sbt clean assembly
mkdir -p build
cp master/target/scala-2.12/master.jar build/master.jar
cp agent/target/scala-2.12/agent.jar build/agent.jar
