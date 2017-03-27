package se.meldrum.spaceturtle

import se.meldrum.spaceturtle.utils.SpaceTurtleConfig


class MainSpec extends BaseSpec with SpaceTurtleConfig {

  test("That port is set when command line argument is sent") {
    Main.main(Array(spaceTurtlePort.toString))
    assert(Main.port match {
      case None => false
      case Some(_) => true
    })
  }
}
