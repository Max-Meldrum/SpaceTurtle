name := "SpaceTurtle." + "agent"

mainClass in (Compile, run) := Some("agent.AgentSystem")
logLevel in assembly := Level.Error
