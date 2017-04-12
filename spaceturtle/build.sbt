name := "SpaceTurtle." + "spaceturtle"

mainClass in (Compile, run) := Some("spaceturtle.Main")

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