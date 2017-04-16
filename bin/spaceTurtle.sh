#! /bin/sh

# TODO: Improve with checks that config exist, and that the jar files also are made.
java -jar -Dconfig.file=conf/spaceturtle.conf -Djava.security.auth.login.config=conf/jaas.conf -Djava.security.krb5.conf=conf/krb5.conf build/SpaceTurtle.jar
 
