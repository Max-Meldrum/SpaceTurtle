#! /bin/bash

git config --global alias.root 'rev-parse --show-toplevel'
REPO_ROOT=$(git root)


[ -f "$REPO_ROOT"/conf/spaceturtle.conf ] || { echo "spaceturtle conf not found!"; exit 1; }
[ -f "$REPO_ROOT"/conf/jaas.conf ] || { echo "Jaas conf not found!"; exit 1; }
[ -f "$REPO_ROOT"/conf/krb5.conf ] || { echo "Kerberos conf not found!" ; exit 1; }
[ -f "$REPO_ROOT"/build/SpaceTurtleCli.jar ] || { echo "Jar file not found, run compile.sh!" ; exit 1; }


java -jar -Dconfig.file="$REPO_ROOT"/conf/spaceturtle.conf -Djava.security.auth.login.config="$REPO_ROOT"/conf/jaas.conf -Djava.security.krb5.conf="$REPO_ROOT"/conf/krb5.conf "$REPO_ROOT"/build/SpaceTurtleCli.jar
