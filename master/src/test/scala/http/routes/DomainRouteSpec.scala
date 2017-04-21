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
import zookeeper.ZkClient.AgentAlias
import zookeeper.{Agent, ZkClient, ZkPaths, ZkSetup}

import scala.util.{Failure, Success}

class DomainRouteSpec extends HttpSpec with ZkPaths with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    ZkSetup.run()
  }

  override def afterAll(): Unit = {
    ZkSetup.clean()
  }

  import master.http.JsonSupport._

  "Domain route " should {
    "not handle GET requests on invalid paths" in {
      Get("/api/v1/domain/invalid") ~> route ~> check {
        handled shouldBe false
      }
    }

    "check health of domain" in {
      Get("/api/v1/domain/health") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "up"
      }
    }

    "get list of active agents" in {
      val testAgent = Agent("testHost", 4, 200000, "QEMU")
      assert(!ZkClient.pathExists(agentSessionPath + "/" + testAgent.host))
      ZkClient.joinCluster(testAgent) match {
        case Success(_) => {
          Get("/api/v1/domain/agents/active") ~> route ~> check {
            status shouldEqual StatusCodes.OK
            responseAs[List[AgentAlias]] shouldEqual List(testAgent.host)
          }
        }
        case Failure(e) => {
          println(e.toString)
          fail("Could not join cluster")
        }
      }
    }
  }
}
