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
import zookeeper.{Agent, ZkClient}

import scala.util.{Failure, Success}

class DomainRouteSpec extends HttpSpec {

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
      ZkClient.createPath("/agents")
      assert(ZkClient.pathExists("/agents"))

      ZkClient.joinCluster("localhost", "testAgent") match {
        case Success(_) => {
          Get("/api/v1/domain/agents/active") ~> route ~> check {
            status shouldEqual StatusCodes.OK
            responseAs[List[Agent]] shouldEqual List(Agent("testAgent"))
          }
        }
        case Failure(_) => fail("Could not join cluster")
      }
    }
  }
}
