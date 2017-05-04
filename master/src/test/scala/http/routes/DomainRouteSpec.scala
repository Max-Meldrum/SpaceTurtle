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

import master.HttpSpec
import org.scalatest.BeforeAndAfterAll
import io.circe.syntax._
import zookeeper.{Agent, Domain, ZkPaths, ZkSetup}

class DomainRouteSpec extends HttpSpec with ZkPaths with BeforeAndAfterAll {
  val testAgent = Agent("testHost", 4, 200000, "QEMU")

  override def beforeAll(): Unit = ZkSetup.run()
  override def afterAll(): Unit = ZkSetup.clean()

  // JSON marshalling/unmarshalling
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  "Domain route " should {
    "not handle GET requests on invalid paths" in {
      Get("/api/v1/domain/invalid") ~> route ~> check {
        handled shouldBe false
      }
    }

    "insert domain" in {
      val domain = Domain("Debian test", "test machine", "KVM", "test", "test", 12000, 2)
      val request = postRequest("/api/v1/domain/create", domain.asJson.noSpaces)

      request ~> route ~> check {
        //responseAs[List[Domain]] shouldEqual List(domain)
        responseAs[String] shouldEqual "hej"
      }
    }
  }

}
