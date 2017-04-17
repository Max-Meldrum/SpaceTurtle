#! /bin/bash

git config --global alias.root 'rev-parse --show-toplevel'
REPO_ROOT=$(git root)


# Collects the latest keytab from the container

docker cp zookeeper:/opt/zookeeper-3.5.2-alpha/conf/zookeeper-client.keytab "$REPO_ROOT"/conf/zookeeper-client.keytab

