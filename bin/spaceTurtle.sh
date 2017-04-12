#! /bin/sh

# TODO: Improve with checks that config exist, and that the jar files also are made.
java -jar -Dconfig.file=conf/spaceturtle.conf build/SpaceTurtle.jar
