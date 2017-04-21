name := "SpaceTurtle." + "master"

mainClass in (Compile, run) := Some("master.Master")

logLevel in assembly := Level.Error


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
