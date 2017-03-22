package se.meldrum.spaceturtle

import se.meldrum.spaceturtle.network.server.SpaceTurtleServer


object Main extends App {
  SpaceTurtleServer.run(8080)
}
