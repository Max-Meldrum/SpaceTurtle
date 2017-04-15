#! /bin/bash

# Set up container
docker run -d --name zookeeper --net=host meldrum/docker-zookeeper-kerberos

# TODO: Fix this
echo "Waiting while kerberos load data..."
sleep 20
# Copy our Client JaaS file over to conf directory
docker cp zookeeper:/opt/zookeeper-3.5.2-alpha/conf/zookeeper-client.keytab ../conf/zookeeper-client.keytab

