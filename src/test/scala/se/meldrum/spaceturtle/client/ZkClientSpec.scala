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

package se.meldrum.spaceturtle.client

import org.scalatest.BeforeAndAfterAll
import se.meldrum.spaceturtle.{BaseSpec, ZkTestClient}
import se.meldrum.spaceturtle.network.client.ZkClient
import se.meldrum.spaceturtle.network.server.ZkSetup
import se.meldrum.spaceturtle.utils.{ZkUtils, ZkPaths}

import scala.util.{Failure, Success, Try}


class ZkClientSpec extends BaseSpec with ZkPaths with BeforeAndAfterAll {
  implicit val zkClient = ZkTestClient.zkCuratorFrameWork

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

  test("That agent host and port name is correct") {
    val byteData= zkClient.getData().forPath(spaceTurtleUserPath)
    val zkData = new String(byteData)
    val parsedZkData = zkData.split(" \\r?\\n")
      .map(_.trim)
      .mkString

    val zNodeHost = parsedZkData.split("\\r?\\n")(0)
      .split("Host=")
      .mkString

    val zNodePort= parsedZkData.split("\\r?\\n")(1)
      .split("Port=")
      .mkString

    assert(zNodePort == spaceTurtlePort.toString)
    assert(zNodeHost == spaceTurtleHost)
  }

  test("That we are using an empheral node") {
    val stat = Option(zkClient.checkExists().forPath(spaceTurtleUserPath))

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
    val t = Try(zkClient.start())
    t match {
      case Success(_) => fail("We are already started, should end up in a fail")
      case Failure(e) => assert(zkClient.getZookeeperClient.isConnected)
    }
  }

  test ("That we can fetch a list of active agents") {
    val agents = ZkClient.getAgents()
    assert(!agents.isEmpty)
  }

}
