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

import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.api.ACLProvider
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.{CreateMode, ZooDefs}
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConverters._
import scala.util.Try

/** ZooKeeper Client
  *
  * Uses application.conf and CuratorFramework to build a
  * client
  */
trait ZkClient extends ZooKeeperConfig {
  val zkRetryPolicy = new ExponentialBackoffRetry(1000, zkMaxReconnections)
  val zkCuratorFrameWork = CuratorFrameworkFactory.builder()
    .namespace(zkNamespace)
    .connectString(zkHost)
    .retryPolicy(zkRetryPolicy)
    .sessionTimeoutMs(zkConnectionTimeout)
    .connectionTimeoutMs(zkSessionTimeout)
    .aclProvider(new ACLProvider {
      override def getDefaultAcl: java.util.List[ACL] = ZooDefs.Ids.CREATOR_ALL_ACL
      override def getAclForPath(path: String): java.util.List[ACL] = ZooDefs.Ids.CREATOR_ALL_ACL
    })
    .build()
}

object ZkClient extends ZkClient with ZkPaths with LazyLogging {
  type ZooKeeperClient = CuratorFramework
  type AgentAlias = String


  /** Attempts to connect to the ZooKeeper ensemble
    *
    * @return True if connected, otherwise false
    */
  def connect()(implicit zk: ZooKeeperClient): Boolean = {
    zk.start()
    // Short sleep time for letting it try to connect
    Thread.sleep(500)
    isConnected()
  }

  /** Checks connection to ZooKeeper
    *
    * @param zk ZooKeeper client
    * @return true if connected, false otherwise
    */
  def isConnected()(implicit zk: ZooKeeperClient): Boolean =
    zk.getZookeeperClient.isConnected

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
  def createPath(path: String)(data: String = "")(implicit zk: ZooKeeperClient) : Unit = {
    pathExists(path) match {
      case false => zk.create().forPath(path, data.getBytes)
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

  /** Joins SpaceTurtle cluster
    *
    * @param agent Agent case class which holds all information
    * @param zk ZooKeeper client
    */
  def joinCluster(agent: Agent)(implicit zk: ZooKeeperClient): Try[Unit] = {
    val path = agentSessionPath + "/" + agent.host
    // EPHEMERAL means the data will get deleted after session is lost
    Try(zk.create().withMode(CreateMode.EPHEMERAL).forPath(path))
  }

  /** Registers agent if it has not has already
    *
    * @param agent Agent case class
    * @param zk ZooKeeper client
    */
  def registerAgent(agent: Agent)(implicit zk: ZooKeeperClient): Unit = {
    val path = agentPersistedPath + "/" + agent.host
    if (!pathExists(path)) {
      val host = "host=" + agent.host+ "\n"
      val cpus = "cpus=" + agent.cpus + "\n"
      val totalMem = "totalMem=" + agent.totalMem + "\n"
      val virtualType = "type=" + agent.virtualType
      val input = (host + cpus + totalMem + virtualType)
      createPath(path)(input)
    }
  }

  /** Fetch active agents
    *
    * @param zk ZooKeeper client
    * @return list of agents
    */
  def getAgentNames()(implicit zk: ZooKeeperClient): List[AgentAlias] = {
    // Ensure we are getting latest commits
    zk.sync().forPath(agentSessionPath)

    zk.getChildren
      .forPath(agentSessionPath)
      .asScala
      .toList
  }

  /** Fetch information for specified agent
    *
    * @param path target agent
    * @param zk ZooKeeper client
    * @return Agent case class with information about the target
    */
  def getAgentInformation(path: String)(implicit zk: ZooKeeperClient): Agent = {
    val byteData= zk.getData().forPath(path)
    val zkData = new String(byteData)
    parseAgentNode(zkData)
  }

  /** Parse znode Data
    *
    * @param zkData Data that has been fetched from a client.getData().forPath()
    * @return Agent case class that holds information about the agent
    */
  def parseAgentNode(zkData: String): Agent = {
    val parsedZkData = zkData.split(" \\r?\\n")
      .map(_.trim)
      .mkString

    // TODO: Refactor

    val host= parsedZkData.split("\\r?\\n")(0)
      .split("host=")
      .mkString

    val cpus = parsedZkData.split("\\r?\\n")(1)
      .split("cpus=")
      .mkString
      .toInt

    val totalMem = parsedZkData.split("\\r?\\n")(2)
      .split("totalMem=")
      .mkString
      .toLong

    val virtualType = parsedZkData.split("\\r?\\n")(3)
      .split("type=")
      .mkString

    Agent(host, cpus, totalMem, virtualType)
  }

}

