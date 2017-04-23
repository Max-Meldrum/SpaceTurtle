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

package zookeeper

import zookeeper.ZkClient.ZooKeeperClient

object ZkSetup extends ZkPaths {

  /** Set up znodes that we require
    *
    * @param zk ZooKeeper client
    */
  def run()(implicit zk: ZooKeeperClient): Unit = {
    create(agentsPath)
    create(agentPersistedPath)
    create(agentSessionPath)
  }

  /** Clean znodes
    *
    * @param zk ZooKeeper client
    */
  def clean()(implicit zk: ZooKeeperClient): Unit = {
    delete(agentsPath)
    delete(agentPersistedPath)
    delete(agentSessionPath)
  }

  private def create(path: String)(implicit zk: ZooKeeperClient): Unit = {
    ZkClient.nodeExists(path) match {
      case true =>
      case false => ZkClient.createNode(path)
    }
  }

  private def delete(path: String)(implicit zk: ZooKeeperClient): Unit = {
    ZkClient.nodeExists(path) match {
      case true => ZkClient.deleteNode(path)
      case false =>
    }
  }


}
