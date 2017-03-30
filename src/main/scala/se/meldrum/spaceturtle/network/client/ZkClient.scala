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

package se.meldrum.spaceturtle.network.client


import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.CreateMode
import se.meldrum.spaceturtle.utils.{ZkConfig, ZkPaths}
import scala.collection.JavaConverters._


/** ZooKeeper Client
  *
  * Uses application.conf and CuratorFramework to build a
  * client
  */
trait ZkClient extends ZkConfig {
  val zkRetryPolicy = new ExponentialBackoffRetry(1000, zkMaxReconnections)
  val zkCuratorFrameWork = CuratorFrameworkFactory.builder()
    .namespace(zkNamespace)
    .connectString(zkHost)
    .retryPolicy(zkRetryPolicy)
    .sessionTimeoutMs(zkConnectionTimeout)
    .connectionTimeoutMs(zkSessionTimeout)
    .build()
}

object ZkClient extends ZkClient with ZkPaths {

  type AgentAlias = String

  /** Attempts to connect to the ZooKeeper ensemble
    *
    * If it fails, it will try using the RetryPolicy,
    * the attempts are decided by zkMaxReconnections
    */
  def connect()(implicit zkClient: CuratorFramework): Unit = zkClient.start()


  /** Joins SpaceTurtle cluster
    *
    * @param host IP/hostname to allow people to connect to us
    * @param user Username for cluster
    * @param port Port that our server listens on
    * @param zkClient Allows us to easily call this method with the TestingServer as well
    */
  def joinCluster(host: String, user: String, port: Int)(implicit zkClient: CuratorFramework) : Unit = {
    val zHost = "Host=" + host + "\n"
    val zPort = "Port=" + port.toString
    val path = agentPath + "/" + user
    val input = (zHost + zPort).getBytes
    // EPHEMERAL means the data will get deleted after session is lost
    zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, input)
  }

  /** Fetch active agents
    *
    * @param zkClient Allows us to easily call this method with the TestingServer as well
    * @return list of agents represented in a case class
    */
  def getAgents()(implicit zkClient: CuratorFramework): List[AgentAlias] = {
    // Ensure we are getting latest commits
    zkClient.sync().forPath(agentPath)

    zkClient.getChildren
      .forPath(agentPath)
      .asScala
      .toList
  }
}
