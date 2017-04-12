import sbt._

object Dependencies {

  val scalaTestVersion = "3.0.1"
  val curatorVersion = "3.3.0"
  val nettyVersion = "4.0.4.Final"
  val scalaLoggingVersion = "3.5.0"
  val logbackVersion = "1.1.7"
  val typeConfigVersion = "1.3.1"


  val spaceTurtleDependencies : Seq[ModuleID] = Seq(
    "io.netty" % "netty-all" % nettyVersion,
    "org.apache.curator" % "curator-framework" % curatorVersion,
    "org.apache.curator" % "curator-test" % curatorVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.typesafe" % "config" % typeConfigVersion
  )

  val cliDependencies : Seq[ModuleID] = spaceTurtleDependencies

}