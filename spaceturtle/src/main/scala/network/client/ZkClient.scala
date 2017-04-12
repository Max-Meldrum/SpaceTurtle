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

package spaceturtle.network.client


import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.CreateMode
import spaceturtle.utils._

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
  type ZooKeeperClient = CuratorFramework

  /** Attempts to connect to the ZooKeeper ensemble
    *
    * If it fails, it will try using the RetryPolicy,
    * the attempts are decided by zkMaxReconnections
    */
  def connect()(implicit zk: ZooKeeperClient): Unit = zk.start()


  /** Joins SpaceTurtle cluster
    *
    * @param host IP/hostname to allow people to connect to us
    * @param user Username for cluster
    * @param port Port that our server listens on
    * @param zk ZooKeeper client
    */
  def joinCluster(host: String, user: String, port: Int)(implicit zk: ZooKeeperClient) : Unit = {
    val zHost = "Host=" + host + "\n"
    val zPort = "Port=" + port.toString
    val path = agentPath + "/" + user
    val input = (zHost + zPort).getBytes
    // EPHEMERAL means the data will get deleted after session is lost
    zk.create().withMode(CreateMode.EPHEMERAL).forPath(path, input)
  }

  /** Fetch active agents
    *
    * @param zk ZooKeeper client
    * @return list of agents represented in a case class
    */
  def getAgentNames()(implicit zk: ZooKeeperClient): List[AgentAlias] = {
    // Ensure we are getting latest commits
    zk.sync().forPath(agentPath)

    zk.getChildren
      .forPath(agentPath)
      .asScala
      .toList
  }

  /** Checks connection to ZooKeeper
    *
    * @param zk ZooKeeper client
    * @return true if connected, false otherwise
    */
  def isConnected()(implicit zk: ZooKeeperClient): Boolean =
    zk.getZookeeperClient.isConnected


  /** Fetch information for specified agent
    *
    * @param path target agent
    * @param zk ZooKeeper client
    * @return Agent case class with information about the target
    */
  def getAgentInformation(path: String)(implicit zk: ZooKeeperClient): Agent = {
    val byteData= zk.getData().forPath(spaceTurtleUserPath)
    val zkData = new String(byteData)
    ZkUtils.parseUserAgentNode(zkData)
  }

  /** Creates a socket connection to each client and sends msg
    *
    * @param msg what is to be sent
    * @param zk ZooKeeper client
    */
  def announceClusterMessage(msg: String)(implicit zk: ZooKeeperClient): String = {
    val agentNames = getAgentNames()
    val agents = agentNames.map(getAgentInformation)
    agentNames.foreach { c =>
      println
    }
    agents.isEmpty match {
      case true => "No available agents"
      case false => {
        agents.foreach(new SpaceTurtleClient(_).run(msg))
        "Sending to agents"
      }
    }
  }

  /** Send file to all agents in the cluster
    *
    * @param f case class with the file, name and length
    * @return
    */
  def sendFile(f: FileContainer)(implicit zk: ZooKeeperClient): Unit = {
    val agentNames = getAgentNames()
    val agents = agentNames.map(getAgentInformation(_))

    agents.isEmpty match {
      case true => "No available agents"
      case false => {
        //agents.foreach(new SpaceTurtleClient().transferFile(f))
      }
    }
  }
}
