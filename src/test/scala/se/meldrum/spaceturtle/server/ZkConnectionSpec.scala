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

package se.meldrum.spaceturtle.server

import org.scalatest.BeforeAndAfterAll
import se.meldrum.spaceturtle.network.server.ZkConnection
import se.meldrum.spaceturtle.{BaseSpec, ZkTestClient}


class ZkConnectionSpec extends BaseSpec with BeforeAndAfterAll {
  implicit val zkClient = ZkTestClient.zkCuratorFrameWork
  val user = "turtle"
  val port = 2000
  val host = "localhost"
  val zNodePath = "/agents/" + user

  override def beforeAll(): Unit = {
    ZkTestClient.nodeSetup()
  }

  override def afterAll(): Unit = {
    ZkTestClient.cleanZnodes()
  }

  test("That agent joins cluster") {
    ZkConnection.joinCluster(host, user, port)
    val znodePath = "/agents/" + user
    val result = zkClient.checkExists().forPath(znodePath)
    assert(ZkTestClient.pathExists(result))
  }

  test("That agent host and port name is correct") {
    val byteData= zkClient.getData().forPath(zNodePath)
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

    assert(zNodePort == port.toString)
    assert(zNodeHost == host)
  }

  test("That we are using an empheral node") {
    // TODO check that we have a session
  }

}
