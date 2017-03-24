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

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import se.meldrum.spaceturtle.utils.ZkConfig

import scala.util.Try

/**
  * Object which holds the ZooKeeper Curator client
 */
object ZkConnection extends ZkConfig {
  private val retryPolicy = new ExponentialBackoffRetry(1000, 3)
  private val client = CuratorFrameworkFactory.newClient(host, retryPolicy)

  /** Attempts to connect to the ZooKeeper ensemble
    *
    * @return A Scala try which we can match on to see if connection succeeded
    */
  def connect(): Try[Unit] = {
    Try {
      client.start()
      assert(client.getZookeeperClient.isConnected)
    }
  }

  /** Closes CuratorFramework
    *
    * When we are done using the client, we need to close it
    */
  def close(): Unit = client.close()
}
