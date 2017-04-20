name := "SpaceTurtle." + "root"

lazy val masterSettings = Seq(
  version := "0.1",
  organization := "se.meldrum.spaceturtle",
  scalaVersion := "2.12.1",
  fork in run := true,
  fork in Test := true,
  cancelable in Global := true,
    javaOptions in run ++= Seq(
    "-Dconfig.file=../conf/master.conf",
    "-Djava.security.auth.login.config=../conf/jaas.conf",
    "-Djava.security.krb5.conf=../conf/krb5.conf"
  )
)

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "se.meldrum.spaceturtle",
  scalaVersion := "2.12.1",
  fork in run := true,
  javaOptions in run ++= Seq(
    "-Dconfig.file=../conf/spaceturtle.conf",
    "-Djava.security.auth.login.config=../conf/jaas.conf",
    "-Djava.security.krb5.conf=../conf/krb5.conf"
  )
)

lazy val spaceturtle = (project in file("spaceturtle"))
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("spaceturtle.Main"),
    assemblyJarName in assembly := "SpaceTurtle.jar" ,
    libraryDependencies ++= Dependencies.spaceTurtleDependencies
  )

lazy val cli = (project in file("cli"))
  .dependsOn(spaceturtle  % "test->test;compile->compile")
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("cli.SpaceTurtleCli"),
    assemblyJarName in assembly := "SpaceTurtleCli.jar",
    libraryDependencies ++= Dependencies.cliDependencies,
    connectInput in run := true
  )

lazy val master = (project in file("master"))
  .dependsOn(zookeeper % "test->test;compile->compile")
  .settings(masterSettings: _*)
  .settings(
    mainClass in assembly := Some("master.Master"),
    assemblyJarName in assembly := "SpaceTurtleMaster.jar" ,
    libraryDependencies ++= Dependencies.spaceTurtleMasterDependencies
  )

lazy val zookeeper = (project in file("zookeeper"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.zookeeperDependencies
  )

lazy val root = (project in file("."))
  .aggregate(cli, master, spaceturtle, zookeeper)


