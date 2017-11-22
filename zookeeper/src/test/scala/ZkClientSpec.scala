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
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class ZkClientSpec extends BaseSpec with ZkPaths
  with BeforeAndAfterAll with ZkSpec {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  test("That agent joins cluster") {
    ZkClient.joinCluster(testAgent)
    assert(ZkClient.nodeExists(testSessionPath))
  }

  test("That we can register agent") {
    ZkClient.registerAgent(testAgent)
    assert(ZkClient.nodeExists(testPersistentPath))
  }

  test("That we can fetch persisted agent information") {
    val agent = Await.result(ZkClient.getAgent(testAgent.host).map(_.right.toOption), 2 seconds)
    agent match {
      case Some(a) => {
        assert(a.host == testAgent.host)
      }
      case None => fail("Failed fetching agent")
    }
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

  test("That we can fetch a list of active agent names") {
    val agents = Await.result(ZkClient.activeAgents(), 2 seconds)
    assert(!agents.isEmpty)
  }

  test("That we can fetch a list of persisted agent names") {
    val agents = Await.result(ZkClient.persistedAgents(), 2 seconds)
    assert(!agents.isEmpty)
  }

  test("That we can fetch list of Agent case classes") {
    val agents = Await.result(ZkClient.persistedAgentsFull(), 2 seconds)
    assert(agents.contains(testAgent))
  }
}
