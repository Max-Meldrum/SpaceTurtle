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

import org.apache.zookeeper.data.Stat
import se.meldrum.spaceturtle.{BaseSpec, ZkTestClient}


class ZkSetupSpec extends BaseSpec with ZkTestClient {
  private val zkClient = ZkTestClient.zkCuratorFrameWork

  test("Check that Agents node gets created") {
    val firstCheck: Stat = zkClient.checkExists().forPath("/agents")
    assert(pathExists(firstCheck) == false)

    val agentNode = zkClient.create().forPath("/agents")
    assert(agentNode == "/agents")

    val finalCheck = zkClient.checkExists().forPath("/agents")
    assert(pathExists(finalCheck) == true)
  }

  /** Check if Znode path exists
    *
    * @param stat On null, it means we could not find the path
    * @return True if it exist, otherwise false
    */
  def pathExists(stat: Stat): Boolean = {
    stat match {
      case null => false
      case _ => true
    }
  }
}
