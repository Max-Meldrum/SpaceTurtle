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

package agent.vm

import agent.BaseSpec

import scala.util.{Failure, Success}

class LibVirtSpec extends BaseSpec {

  test("Libvirt is able to initialize") {
    LibVirt.init() match {
      case Success(conn) => assert(conn.isConnected)
      case Failure(_) => fail("Failed starting libvirt Connect")
    }
  }

  test("Load agent info") {
    LibVirt.init() match {
      case Success(conn) => {
        val agent = LibVirt.getAgentInfo(conn)
        assert(agent.host == conn.getHostName)
        assert(agent.cpus == conn.nodeInfo().cpus)
        assert(agent.totalMem == conn.nodeInfo().memory)
        assert(agent.virtualType == conn.getType)
      }
      case Failure(_) => fail("Failed starting libvirt Connect")
    }
  }
}
