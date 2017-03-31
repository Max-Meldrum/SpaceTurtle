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

package se.meldrum.spaceturtle.utils

import com.typesafe.scalalogging.LazyLogging
import se.meldrum.spaceturtle.network.client.ZkClient.ZooKeeperClient


object ZkUtils extends LazyLogging {

  /** Check if Znode path exists
    *
    * @param path znode path
    * @return True if it exist, otherwise false
    */
  def pathExists(path: String)(implicit zk: ZooKeeperClient): Boolean = {
    val stat = Option(zk.checkExists().forPath(path))
    stat match {
      case None => false
      case Some(_) => true
    }
  }

  /** Creates znode if not exists
    *
    * @param path target path
    * @param zk ZooKeeper client
    */
  def createPath(path: String)(implicit zk: ZooKeeperClient) : Unit = {
    pathExists(path) match {
      case false => zk.create().forPath(path)
      case true => logger.info("Path already exists " + path)
    }
  }

  /** Deletes ZooKeeper znode
    *
    * @param path target path
    * @param zk ZooKeeper client
    */
  def deleteZNode(path: String)(implicit zk: ZooKeeperClient): Unit = {
    pathExists(path) match {
      case true => zk.delete().deletingChildrenIfNeeded().forPath(path)
      case false => logger.info("Tried deleting a non existing path: " + path)
    }
  }

  /** Parse Znode Data
    *
    * @param zkData Data that has been fetched from a client.getData().forPath()
    * @return Agent case class that holds information about the agent
    */
  def parseUserAgentNode(zkData: String): Agent = {
    val parsedZkData = zkData.split(" \\r?\\n")
      .map(_.trim)
      .mkString

    val agentHost = parsedZkData.split("\\r?\\n")(0)
      .split("Host=")
      .mkString

    val agentPort = parsedZkData.split("\\r?\\n")(1)
      .split("Port=")
      .mkString

    // TODO: Fetch username as well
    Agent("hej", agentHost, agentPort.toInt)
  }
}
