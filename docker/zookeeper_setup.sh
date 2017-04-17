#! /bin/bash

# Set up container
docker run -d --name zookeeper -h localhost -p 2181:2181 -p 88:88/udp -p 88:88/tcp -p 749:749 meldrum/docker-zookeeper-kerberos

echo "Might take a few seconds for Kerberos to load random data"
echo "Run docker logs zookeeper to check if ZooKeeper is up or not, then run ./fetch_keytab.sh to place keytab in the conf folder"
