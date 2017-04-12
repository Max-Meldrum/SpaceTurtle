/*
 * Copyright 2017 Max Meldrum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spaceturtle.client

import org.scalatest.BeforeAndAfterAll
import spaceturtle.network.client.ZkClient
import spaceturtle.network.server.ZkSetup
import spaceturtle.utils.{ZkPaths, ZkUtils}
import spaceturtle.{BaseSpec, ZkTestClient}
import scala.util.{Failure, Success, Try}


class ZkClientSpec extends BaseSpec with ZkPaths with BeforeAndAfterAll {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = {
    ZkSetup.run()
  }

  override def afterAll(): Unit = {
    ZkSetup.clean()
  }

  test("That agent joins cluster") {
    ZkClient.joinCluster(spaceTurtleHost, spaceTurtleUser, spaceTurtlePort)
    assert(ZkUtils.pathExists(spaceTurtleUserPath))
  }

  test("That we can fetch agent information") {
    val agent = ZkClient.getAgentInformation(spaceTurtleUserPath)

    assert(agent.port == spaceTurtlePort)
    assert(agent.hostName == spaceTurtleHost)
  }

  test("That we are using an empheral node") {
    val stat = Option(zk.checkExists().forPath(spaceTurtleUserPath))

    stat match {
      case None => fail("Could not get stat for " + spaceTurtleUserPath)
      case Some(s) => {
        Option(s.getEphemeralOwner) match {
          case None => fail("We don't have a session, fail")
          case Some(_) => assert(true)
        }
      }
    }
  }

  test("That our CuratorFramework is already started") {
    val t = Try(ZkClient.connect())
    t match {
      case Success(_) => fail("We are already started, should end up in a fail")
      case Failure(e) => assert(ZkClient.isConnected())
    }
  }

  test("That we can fetch a list of active agents") {
    val agents = ZkClient.getAgentNames()
    assert(!agents.isEmpty)
  }
}