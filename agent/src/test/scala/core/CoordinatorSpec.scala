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
import utils.{Leader, Worker}
import zookeeper.{BaseSpec, ZkSetup, ZkSpec, ZkTestClient}


class CoordinatorSpec extends BaseSpec with BeforeAndAfterAll with ZkSpec {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()


  test("Coordinator setup") {
    val leaderElection = new LeaderElection(testAgent)
    val coordinator = new Coordinator(leaderElection)
    assert(coordinator.getState() == Worker)
    leaderElection.startLatch()
    coordinator.start(coordinator.getState())
    assert(coordinator.getState() == Leader)
    leaderElection.closeLatch()
  }
}
