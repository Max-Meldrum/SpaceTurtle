#! /bin/sh

git config --global alias.root 'rev-parse --show-toplevel'
REPO_ROOT=$(git root)


[ -f "$REPO_ROOT"/conf/agent.conf ] || { echo "agent conf not found!"; exit 1; }
[ -f "$REPO_ROOT"/conf/jaas.conf ] || { echo "Jaas conf not found!"; exit 1; }
[ -f "$REPO_ROOT"/conf/krb5.conf ] || { echo "Kerberos conf not found!" ; exit 1; }
[ -f "$REPO_ROOT"/build/agent.jar ] || { echo "Jar file not found, run compile.sh!" ; exit 1; }

cd $REPO_ROOT/bin

java -jar -Dconfig.file=../conf/agent.conf -Djava.security.auth.login.config=../conf/jaas.conf -Djava.security.krb5.conf=../conf/krb5.conf ../build/agent.jar
