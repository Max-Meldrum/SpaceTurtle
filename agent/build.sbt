name := "SpaceTurtle." + "agent"

mainClass in (Compile, run) := Some("agent.Main")
logLevel in assembly := Level.Error
