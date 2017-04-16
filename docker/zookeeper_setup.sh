#! /bin/bash

# Set up container
docker run -d --name zookeeper -h localhost -p 2181:2181 -p 88:88/udp -p 88:88/tcp -p 749:749 meldrum/docker-zookeeper-kerberos

# TODO: Fix this
echo "Waiting while kerberos load data..."
sleep 20
# Copy our Client JaaS file over to conf directory
docker cp zookeeper:/opt/zookeeper-3.5.2-alpha/conf/zookeeper-client.keytab conf/zookeeper-client.keytab

