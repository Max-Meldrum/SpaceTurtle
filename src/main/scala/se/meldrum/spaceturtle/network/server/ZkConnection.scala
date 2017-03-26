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
import org.apache.zookeeper.CreateMode
import se.meldrum.spaceturtle.network.client.ZkClient
import se.meldrum.spaceturtle.utils.SpaceTurtleConfig


/**
  * Object which holds the ZooKeeper Curator client
 */
object ZkConnection extends LazyLogging with SpaceTurtleConfig {
  val zkClient = ZkClient.zkCuratorFrameWork

  /** Attempts to connect to the ZooKeeper ensemble
    *
    * If it fails, it will try using the RetryPolicy,
    * the attempts are decided by zkMaxReconnections
    */
  def connect(): Unit = zkClient.start()

  /** Closes CuratorFramework
    *
    * When we are done using the client, we need to close it
    */
  private def close(): Unit = zkClient.close()


  /** Joins SpaceTurtle cluster
    *
    * @param host IP/hostname to allow people to connect to us
    * @param user Username for cluster
    * @param port Port that our server listens on
    * @param zkClient Allows us to easily call this function with the TestingServer as well
    */
  def joinCluster(host: String, user: String, port: Int)(implicit zkClient: CuratorFramework) : Unit = {
    val path = "/agents/" + user
    val zHost = "Host=" + host + "\n"
    val zPort = "Port=" + port.toString
    val input = (zHost + zPort).getBytes
    // EPHEMERAL means the data will get deleted after session is lost
    zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, input)
  }
}
