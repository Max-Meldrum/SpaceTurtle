#! /bin/bash

docker stop zookeeper && docker rm zookeeper
rm ../conf/zookeeper-client.keytab
