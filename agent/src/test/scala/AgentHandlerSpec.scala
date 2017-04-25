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

package agent

import agent.vm.LibVirt
import org.scalatest.BeforeAndAfterAll
import zookeeper.{BaseSpec, ZkClient, ZkSetup, ZkTestClient}

class AgentHandlerSpec extends BaseSpec with BeforeAndAfterAll {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  test("Path cache listener responds correctly to znode add") {
    val connect = LibVirt.init().getOrElse(fail())
    val handler = new AgentHandler(connect)
    val agent = LibVirt.getAgentInfo(connect)
    ZkClient.registerAgent(agent)
    // Check that the cache has no data
    assert(handler.getCache().getCurrentData().size == 0)
    handler.createAgentCache()
    val path = handler.agentPersistedPath + "/" + connect.getHostName + "/" + "znode"

    ZkClient.createNode(path, Some("test"))
    Thread.sleep(500)
    // After creating something on the path we registered, we should have new size
    assert(handler.getCache().getCurrentData().size == 1)
  }

}
