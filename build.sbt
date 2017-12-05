name := "SpaceTurtle." + "root"

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "se.meldrum.spaceturtle",
  scalaVersion := "2.12.1",
  fork in run := true,
  fork in Test := true,
  cancelable in Global := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case x => MergeStrategy.first
  }
)

lazy val zookeeperSettings = javaOptions in run ++= Seq(
  "-Djava.security.auth.login.config=../conf/jaas.conf",
  "-Djava.security.krb5.conf=../conf/krb5.conf"
)

lazy val masterSettings = commonSettings ++ zookeeperSettings ++ Seq(
    javaOptions in run ++= Seq(
    "-Dconfig.file=../conf/master.conf"
  )
)

lazy val agentSettings = commonSettings ++ zookeeperSettings ++ Seq(
  javaOptions in run ++= Seq(
    "-Dconfig.file=../conf/agent.conf"
  )
)

lazy val agent = (project in file("agent"))
  .dependsOn(zookeeper % "test->test;compile->compile")
  .settings(agentSettings: _*)
  .settings(
    mainClass in assembly := Some("agent.AgentSystem"),
    assemblyJarName in assembly := "agent.jar" ,
    libraryDependencies ++= Dependencies.agentDependencies
  )

lazy val cli = (project in file("cli"))
  .dependsOn(agent % "test->test;compile->compile")
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("cli.SpaceTurtleCli"),
    assemblyJarName in assembly := "cli.jar",
    libraryDependencies ++= Dependencies.cliDependencies,
    connectInput in run := true
  )

lazy val master = (project in file("master"))
  .dependsOn(zookeeper % "test->test;compile->compile")
  .settings(masterSettings: _*)
  .settings(
    mainClass in assembly := Some("master.Master"),
    assemblyJarName in assembly := "master.jar" ,
    libraryDependencies ++= Dependencies.masterDependencies
  )

lazy val zookeeper = (project in file("zookeeper"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.zookeeperDependencies
  )

lazy val root = (project in file("."))
  .aggregate(cli, master, agent, zookeeper)


