#! /bin/sh

java -jar -Dconfig.file=conf/spaceturtle.conf -Djava.security.auth.login.config=conf/jaas.conf -Djava.security.krb5.conf=conf/krb5.conf build/SpaceTurtleCli.jar
