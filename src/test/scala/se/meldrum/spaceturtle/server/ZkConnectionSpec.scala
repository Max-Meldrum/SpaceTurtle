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

import org.scalatest.BeforeAndAfter
import se.meldrum.spaceturtle.network.server.{ZkConnection, ZkSetup}
import se.meldrum.spaceturtle.{BaseSpec, ZkTestClient}


class ZkConnectionSpec extends BaseSpec with BeforeAndAfter {
  implicit val zkClient = ZkTestClient.zkCuratorFrameWork

  before {
    ZkTestClient.nodeSetup()
  }

  after {
    ZkTestClient.cleanZnodes()
  }

  test("That agent joins cluster correctly") {
    val user = "turtle"
    val port = 2000
    val host = "localhost"

    ZkConnection.joinCluster(host, user, port)
    val znodePath = "/agents/" + user
    val result = zkClient.checkExists().forPath(znodePath)
    assert(ZkTestClient.pathExists(result))
  }
}
