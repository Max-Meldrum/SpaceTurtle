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

package master

import akka.http.scaladsl.testkit.ScalatestRouteTest
import http.RestService
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers, WordSpec}
import zookeeper.ZkTestClient

trait BaseSpec extends FunSuite

trait HttpSpec extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll {
  implicit val zkTestClient = ZkTestClient.zkCuratorFrameWork
  val restService = new RestService()
  val route = restService.route

  override def afterAll(): Unit = zkTestClient.close()
  override def beforeAll(): Unit = zkTestClient.start()

}


