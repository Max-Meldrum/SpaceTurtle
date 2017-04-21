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

import org.libvirt.{Connect, ConnectAuthDefault}
import zookeeper.Agent
import scala.util.Try

object LibVirt {

  /** Initialize connection to KVM/QEMU
    *
    * @return Scala Try with libvirt Connect
    */
  def init(): Try[Connect] = {
    Try{
      val auth = new ConnectAuthDefault
      new Connect("qemu:///system", auth, 0)
    }
  }

  /** Collect agent info
    *
    * @param conn libvirt Connect
    * @return Agent case class
    */
  def getAgentInfo(conn: Connect): Agent = {
    val info = conn.nodeInfo()
    Agent(conn.getHostName, info.cpus, info.memory, conn.getType)
  }

}
