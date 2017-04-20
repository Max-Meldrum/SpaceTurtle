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
    * If it fails, it will try using the RetryPolicy,
    * the attempts are decided by zkMaxReconnections
    */
  def connect()(implicit zk: ZooKeeperClient): Unit = zk.start()

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

  /** Joins SpaceTurtle cluster
    *
    * @param host IP/hostname to agent
    * @param agentName nick of agent
    * @param zk ZooKeeper client
    */
  def joinCluster(host: String, agentName: String)(implicit zk: ZooKeeperClient) : Try[Unit] = {
    val zHost = "Host=" + host + "\n"
    val path = agentPath + "/" + agentName
    val input = zHost.getBytes
    // EPHEMERAL means the data will get deleted after session is lost
    Try(zk.create().withMode(CreateMode.EPHEMERAL).forPath(path, input))
  }

  /** Fetch active agents
    *
    * @param zk ZooKeeper client
    * @return list of agents
    */
  def getAgentNames()(implicit zk: ZooKeeperClient): List[AgentAlias] = {
    // Ensure we are getting latest commits
    zk.sync().forPath(agentPath)

    zk.getChildren
      .forPath(agentPath)
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

    val agentHost = parsedZkData.split("\\r?\\n")(0)
      .split("Host=")
      .mkString

    Agent(agentHost)
  }

}

