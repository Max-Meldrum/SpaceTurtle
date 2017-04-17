#! /bin/bash

git config --global alias.root 'rev-parse --show-toplevel'
REPO_ROOT=$(git root)

docker stop zookeeper && docker rm zookeeper
rm "$REPO_ROOT"/conf/zookeeper-client.keytab
