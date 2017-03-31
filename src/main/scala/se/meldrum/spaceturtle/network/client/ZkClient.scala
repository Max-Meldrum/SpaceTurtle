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
import se.meldrum.spaceturtle.utils.{Agent, ZkConfig, ZkPaths, ZkUtils}
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
    * @param zkClient ZooKeeper client
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
    * @param zkClient ZooKeeper client
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

  /** Checks connection to ZooKeeper
    *
    * @param zkClient ZooKeeper client
    * @return true if connected, false otherwise
    */
  def isConnected()(implicit zkClient: CuratorFramework): Boolean =
    zkClient.getZookeeperClient.isConnected


  /** Fetch information for specified agent
    *
    * @param path target agent
    * @param zkClient ZooKeeper client
    * @return Agent case class with information about the target
    */
  def getAgentInformation(path: String)(implicit zkClient: CuratorFramework): Agent = {
    val byteData= zkClient.getData().forPath(spaceTurtleUserPath)
    val zkData = new String(byteData)
    ZkUtils.parseUserAgentNode(zkData)
  }

  /** Creates a socket connection to each client and sends msg
    *
    * @param msg what is to be sent
    * @param zkClient ZooKeeper client
    */
  def announceClusterMessage(msg: String)(implicit zkClient: CuratorFramework): Unit = {
    val agentNames = getAgents()
    val agents = agentNames.map(getAgentInformation(_))
    agents.foreach(new SpaceTurtleClient(_).run(msg))
  }
}
