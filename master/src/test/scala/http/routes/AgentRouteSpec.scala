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

package http.routes

import akka.http.scaladsl.model.StatusCodes
import master.HttpSpec
import org.scalatest.BeforeAndAfterAll
import utils.ApiVersion
import zookeeper.ZkClient.AgentAlias
import zookeeper.{Agent, ZkClient, ZkPaths, ZkSetup}

import scala.util.{Failure, Success}

class AgentRouteSpec extends HttpSpec with ZkPaths
  with BeforeAndAfterAll with ApiVersion {
  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  // JSON marshalling/unmarshalling
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  "Agent route" should {
    "not handle GET requests on invalid paths" in {
      Get(s"/api/${version}/agents/invalid") ~> route ~> check {
        handled shouldBe false
      }
    }

    "get list of active agents" in {
      assert(!ZkClient.nodeExists(agentSessionPath + "/" + testAgent.host))
      ZkClient.joinCluster(testAgent) match {
        case Success(_) => {
          Get(s"/api/${version}/agents/active") ~> route ~> check {
            status shouldEqual StatusCodes.OK
            responseAs[List[AgentAlias]] shouldEqual List(testAgent.host)
          }
        }
        case Failure(e) => fail("Could not join cluster")
      }
    }

    "get list of persisted agents" in {
      ZkClient.registerAgent(testAgent)
      Get(s"/api/${version}/agents/persisted/names") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[AgentAlias]] shouldEqual List(testAgent.host)
      }
    }

    "get agent information" in {
      Get(s"/api/${version}/agents/persisted/full") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[Agent]] shouldEqual List(testAgent)
      }
    }
  }
}
