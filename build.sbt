name := "SpaceTurtle"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.0.4.Final",
  "org.apache.curator" % "curator-framework" % "3.3.0",
  "org.apache.curator" % "curator-test" % "3.3.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe" % "config" % "1.3.1"
)

parallelExecution in Test := false

testGrouping <<= definedTests in Test map { tests =>
  tests.map { test =>
    import Tests._
    new Group(
      name = test.name,
      tests = Seq(test),
      runPolicy = InProcess)
  }.sortWith(_.name < _.name)
}

// SBT coverage
coverageMinimum := 60
coverageFailOnMinimum := true
