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

package core

import org.scalatest.BeforeAndAfterAll
import zookeeper._


class LeaderElectionSpec extends BaseSpec with BeforeAndAfterAll with ZkSpec {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  private val sleepTime = 150

  test("Simple leader election") {
    val node1 = new LeaderElection(testAgent)
    node1.startLatch()
    Thread.sleep(sleepTime)
    assert(node1.isLeader())
    node1.closeLatch()
  }

  test("Leader takeover") {
    val node1 = new LeaderElection(testAgent)
    node1.startLatch()
    Thread.sleep(sleepTime)
    assert(node1.isLeader())

    val node2 = new LeaderElection(testAgentTwo)
    node2.startLatch()
    Thread.sleep(sleepTime)
    assert(!node2.isLeader())

    // Kill first node and see that node2 takes over as leader..
    node1.closeLatch()
    Thread.sleep(sleepTime)
    assert(node2.isLeader())

    // Clean other latch as well
    node2.closeLatch()
  }
}
