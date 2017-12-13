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

package api

import agent.HttpSpec
import akka.http.scaladsl.model.StatusCodes
import models.{AgentState, GeneralResponse}
import org.scalatest.BeforeAndAfterAll
import utils.AgentConfig
import zookeeper.{Application, ZkSetup}


class AgentServiceSpec extends HttpSpec with AgentConfig
  with BeforeAndAfterAll with Endpoints {
  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  // JSON marshalling/unmarshalling
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  "Agent Service" should {
    "be able to fetch status" in {
      Get(s"/api/$version/$statusEndpoint") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val state = entityAs[AgentState]
        state.version shouldEqual version
        state.role shouldEqual "Worker"
      }
    }

    "be able to register app" in {
      val app = Application("test_app", "1")
      Post(s"/api/$version/$registerEndpoint", app) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val resp = entityAs[GeneralResponse]
        resp.msg shouldEqual "Created"
      }
      Post(s"/api/$version/$registerEndpoint", app) ~> route ~> check {
        val resp = entityAs[GeneralResponse]
        resp.msg shouldEqual "Already exists"
      }
    }
  }

}
