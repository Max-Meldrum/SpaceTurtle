name := "SpaceTurtle." + "master"

mainClass in (Compile, run) := Some("master.Master")

logLevel in assembly := Level.Error
