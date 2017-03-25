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
import org.apache.zookeeper.CreateMode
import se.meldrum.spaceturtle.network.client.ZkClient
import se.meldrum.spaceturtle.utils.SpaceTurtleConfig

import scala.util.Try

/**
  * Object which holds the ZooKeeper Curator client
 */
object ZkConnection extends LazyLogging with SpaceTurtleConfig {
  val zkClient = ZkClient.zkCuratorFrameWork

  /** Attempts to connect to the ZooKeeper ensemble
    *
    * @return A Scala Try which we can match on to see if connection succeeded
    */
  def connect(): Try[Unit] = {
    Try {
      zkClient.start()
      assert(zkClient.getZookeeperClient.isConnected)
    }
  }

  /** Closes CuratorFramework
    *
    * When we are done using the client, we need to close it
    */
  private def close(): Unit = zkClient.close()


  /** Join SpaceTurtle cluster
    *
    * Lets cluster know we are connected
    */
  def joinCluster(): Unit = {
    val path = "/agents/" + spaceTurtleUser
    val host = spaceTurtleHost.getBytes
    // EPHEMERAL means the data will get deleted after session is lost
    zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, host)
  }
}
