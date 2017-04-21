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

package zookeeper

import org.scalatest.BeforeAndAfterAll

import scala.util.{Failure, Success, Try}

class ZkClientSpec extends BaseSpec with ZkPaths with BeforeAndAfterAll {
  implicit val zk = ZkTestClient.zkCuratorFrameWork
  val testAgent = Agent("testHost", 4, 200000, "QEMU")
  val testSessionPath = agentSessionPath + "/" + testAgent.host
  val testPersistentPath = agentPersistedPath + "/" + testAgent.host

  override def beforeAll(): Unit = {
    ZkSetup.run()
  }

  override def afterAll(): Unit = {
    ZkSetup.clean()
  }

  test("That agent joins cluster") {
    ZkClient.joinCluster(testAgent)
    assert(ZkClient.pathExists(testSessionPath))
  }

  test("That we can register agent") {
    ZkClient.registerAgent(testAgent)
    assert(ZkClient.pathExists(testPersistentPath))
  }

  test("That we can fetch persisted agent information") {
    val agent = ZkClient.getAgentInformation(testPersistentPath)
    assert(agent.host == testAgent.host)
    assert(agent.cpus == testAgent.cpus)
    assert(agent.totalMem == testAgent.totalMem)
    assert(agent.virtualType == testAgent.virtualType)
  }

  test("That we are using an empheral node") {
    val stat = Option(zk.checkExists().forPath(testSessionPath))

    stat match {
      case None => fail("Could not get stat for " + testAgent.host)
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