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

package se.meldrum.spaceturtle.network.server

import se.meldrum.spaceturtle.network.client.ZkClient


object ZkSetup {
  private val zkClient = ZkClient.zkCuratorFrameWork

  def run(): Unit = {
    createPath("/agents")
  }

  /** Creates Znode if not exists
    *
    * @param path target path
    */
  def createPath(path: String): Unit = {
    val exists = zkClient.checkExists().forPath(path)
    exists match {
      case null => zkClient.create().forPath(path)
      case _ =>
    }
  }
}
