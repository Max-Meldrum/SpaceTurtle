#! /bin/sh

sbt clean assembly
mkdir -p build
cp spaceturtle/target/scala-2.12/SpaceTurtle.jar build/SpaceTurtle.jar
cp cli/target/scala-2.12/SpaceTurtleCli.jar build/SpaceTurtleCli.jar
