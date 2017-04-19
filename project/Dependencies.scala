/*
 * Copyright 2017 Max Meldrum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object Dependencies {
  val scalaTestVersion = "3.0.1"
  val curatorVersion = "3.3.0"
  val nettyVersion = "4.0.4.Final"
  val scalaLoggingVersion = "3.5.0"
  val logbackVersion = "1.1.7"
  val typeConfigVersion = "1.3.1"
  val akkaHttpVersion = "10.0.5"


  val logDependencies : Seq[ModuleID] = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  val confDependencies : Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % typeConfigVersion
  )

  val testDependencies : Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )

  val curatorDependencies : Seq[ModuleID] = Seq(
    "org.apache.curator" % "curator-framework" % curatorVersion,
    "org.apache.curator" % "curator-test" % curatorVersion
  )

  // Common libs that are used together
  val common : Seq[ModuleID] =
    logDependencies ++ confDependencies ++ testDependencies ++ curatorDependencies

  val zookeeperDependencies: Seq[ModuleID] =
    confDependencies ++ curatorDependencies

  val spaceTurtleDependencies : Seq[ModuleID] = common ++ Seq(
    "io.netty" % "netty-all" % nettyVersion
  )

  val cliDependencies : Seq[ModuleID] = spaceTurtleDependencies

  val spaceTurtleMasterDependencies : Seq[ModuleID] = common ++ Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
  )



}