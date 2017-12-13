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

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import api.AgentService
import core.{Coordinator, LeaderElection}
import org.scalatest.{FunSuite, Matchers, WordSpec}
import zookeeper.{Agent, TestUtils, ZkTestClient}


trait BaseSpec extends FunSuite

trait HttpSpec extends WordSpec with Matchers with ScalatestRouteTest with
  TestUtils {
  implicit val zkTestClient = ZkTestClient.zkCuratorFrameWork
  private[this] val agent = Agent("test", "node-1")
  private[this] val leader = new LeaderElection(agent)
  private[this] val coordinator = new Coordinator(leader)
  val service = new AgentService(coordinator, agent)
  val route = service.route


  def postRequest(path: String, json: String): HttpRequest =
    HttpRequest(HttpMethods.POST,
      uri = path,
      entity = HttpEntity(MediaTypes.`application/json`, json)
    )
}



