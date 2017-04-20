name := "SpaceTurtle." + "agent"

mainClass in (Compile, run) := Some("agent.Main")

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

logLevel in assembly := Level.Error
