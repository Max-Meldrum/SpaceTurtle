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

package spaceturtle.utils

import org.scalatest.BeforeAndAfterAll
import spaceturtle.network.server.ZkSetup
import spaceturtle.{BaseSpec, ZkTestClient}


class UtilSpec extends BaseSpec with ZkPaths with BeforeAndAfterAll {
  implicit val zk = ZkTestClient.zkCuratorFrameWork

  override def beforeAll(): Unit = {
    ZkSetup.run()
  }

  override def afterAll(): Unit = {
    ZkSetup.clean()
  }

}
