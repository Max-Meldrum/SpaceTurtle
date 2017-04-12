name := "SpaceTurtle." + "cli"

mainClass in (Compile, run) := Some("cli.SpaceTurtleCli")

logLevel in assembly := Level.Error

