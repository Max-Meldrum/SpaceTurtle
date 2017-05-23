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
import zookeeper._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class InfrastructureSpec extends BaseSpec with BeforeAndAfterAll {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  //TODO: Refactor
  test("Path cache listener responds to add domain") {
    val connect = LibVirt.init().getOrElse(fail("Failed to init Libvirt"))
    val handler = new Infrastructure(connect)
    val agent = LibVirt.getAgentInfo(connect)
    ZkClient.registerAgent(agent)
    // Check that the cache has no data
    assert(handler.getDomainCache().getCurrentData().size == 0)
    handler.createCache()
    val path = handler.agentPersistedPath + "/" +
      connect.getHostName + "/infrastructure/domain/test"
    val domain = Domain("test", "Test domain", "Domain for unit test", "kvm", "new", 0, 0)

    ZkClient.createNode(path, Some(domain.asJson.noSpaces))
    Thread.sleep(2000)
    // After creating something on the path we registered, we should have new size
    assert(handler.getDomainCache().getCurrentData().size == 1)

    val updatedDomain = Await.result(ZkClient.getDomain(path), 2 seconds)
    assert(updatedDomain.status == "processing")
  }

}
