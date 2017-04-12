name := "SpaceTurtle." + "root"

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "se.meldrum.spaceturtle",
  scalaVersion := "2.12.1",
  test in assembly := {}
)

lazy val spaceturtle = (project in file("spaceturtle")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("spaceturtle.Main"),
    assemblyJarName in assembly := "SpaceTurtle.jar" ,
    libraryDependencies ++= Dependencies.spaceTurtleDependencies
  )

lazy val cli = (project in file("cli"))
  .dependsOn(spaceturtle  % "test->test;compile->compile").
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("cli.SpaceTurtleCli"),
    assemblyJarName in assembly := "SpaceTurtleCli.jar",
    libraryDependencies ++= Dependencies.cliDependencies
  )

lazy val root = (project in file(".")).
  aggregate(cli, spaceturtle)

logLevel in assembly := Level.Error

