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

import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.CuratorFramework
import se.meldrum.spaceturtle.utils.ZkPaths

object ZkSetup extends LazyLogging with ZkPaths {

  def run()(implicit zkClient: CuratorFramework): Unit = {
    logger.info("Creating znodes if they don't exist")
    createPath(agentPath)
  }

  def clean()(implicit zkClient: CuratorFramework): Unit = {
    logger.info("Cleaning znodes, will force delete")
    deleteZNode(agentPath)
  }

  /** Creates znode if not exists
    *
    * @param path target path
    * @param zkClient Allows us to easily call this function with the TestingServer as well
    */
  private def createPath(path: String)(implicit zkClient: CuratorFramework) : Unit = {
    val stat = Option(zkClient.checkExists().forPath(path))

    stat match {
      case None => zkClient.create().forPath(path)
      case Some(_) => logger.info("Could not create path" + path)
    }
  }

  /** Deletes ZooKeeper znode
    *
    * @param path target path
    * @param zkClient Allows us to easily call this function with the TestingServer as well
    */
  private def deleteZNode(path: String)(implicit zkClient: CuratorFramework): Unit = {
    val stat = Option(zkClient.checkExists().forPath(path))

    stat match {
      case None => logger.info("Tried deleting non existing path: " + path)
      case Some(_) => zkClient.delete().deletingChildrenIfNeeded().forPath(path)
    }
  }
}
