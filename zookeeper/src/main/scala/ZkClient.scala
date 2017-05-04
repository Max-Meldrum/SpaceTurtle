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
import io.circe.Error
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.api.ACLProvider
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.{CreateMode, ZooDefs}
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import io.circe.jawn.decode
import io.circe.generic.auto._
import io.circe.syntax._

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

  /** Check if znode exists
    *
    * @param path znode path
    * @return True if it exist, otherwise false
    */
  def nodeExists(path: String)(implicit zk: ZooKeeperClient): Boolean =
    Option(zk.checkExists().forPath(path)).isDefined

  /** Creates ZooKeeper znode
    *
    * @param path target path
    * @param zk ZooKeeper client
    */
  def createNode(path: String, data: Option[String] = None)(implicit zk: ZooKeeperClient) : Unit = {
    nodeExists(path) match {
      case false => zk.create().creatingParentsIfNeeded().forPath(path, data.getOrElse("").getBytes)
      case true => logger.info("Path already exists " + path)
    }
  }

  /** Deletes ZooKeeper znode
    *
    * @param path target path
    * @param zk ZooKeeper client
    */
  def deleteNode(path: String)(implicit zk: ZooKeeperClient): Unit = {
    nodeExists(path) match {
      case true => zk.delete().deletingChildrenIfNeeded().forPath(path)
      case false => logger.info("Tried deleting a non existing path: " + path)
    }
  }

  /** Update znode
    *
    * @param path target path for znode
    * @param data data to set
    * @param zk ZooKeeper client
    */
  def updateNode(path: String, data: Option[String] = None)(implicit zk: ZooKeeperClient): Unit =
    zk.setData().forPath(path, data.getOrElse("").getBytes)

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
    if (!nodeExists(path)) {
      val data = agent.asJson
        .noSpaces

      createNode(path, Some(data))
    }
  }

  /** Fetch persisted agents, including full information
    *
    * @return Future containing List of Agent case classes
    */
  def persistedAgentsFull()(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[List[Agent]] = {
    persistedAgents().flatMap { names =>
      Future.sequence(names.map(n => getAgent(n)))
    }
  }

  /** Fetch active agents
    *
    * @param zk ZooKeeper client
    * @return Future containing list of agent names
    */
  def activeAgents()(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[List[AgentAlias]] =
    Future(fetchNodes(agentSessionPath))

  /** Fetch persisted agents
    *
    * @param zk ZooKeeper client
    * @return Future containing list of persisted agent names
    */
  def persistedAgents()(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[List[AgentAlias]] =
    Future(fetchNodes(agentPersistedPath))

  /** Fetch znodes under certain path
    *
    * @param path znode path /SpaceTurtle/..
    * @param zk ZooKeeper client
    * @return List of found znodes
    */
  private def fetchNodes(path: String)(implicit zk: ZooKeeperClient): List[String] = {
    // Ensure we are getting latest commits
    zk.sync().forPath(path)

    zk.getChildren
      .forPath(path)
      .asScala
      .toList
  }

  /** Fetch information for specified agent
    *
    * @param znode target agent
    * @param zk ZooKeeper client
    * @param ec ExecutionContext for Future
    * @return Future with Agent case class
    */
  def getAgent(znode: String)(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[Agent] = Future {
    val byteData= zk.getData().forPath(agentPersistedPath + "/" + znode)
    val zkData = new String(byteData)
    val agent: Either[Error, Agent] = decode[Agent](zkData)
    agent.getOrElse(Agent("JSON parse error", 2, 2, "fail"))
  }

  /** Fetch information for specified domain
    *
    * @param znode target domain
    * @param zk ZooKeeper Client
    * @param ec ExecutionContext for Future
    * @return Future with Domain case class
    */
  def getDomain(znode: String)(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[Domain] = Future {
    val byteData= zk.getData().forPath(znode)
    val zkData = new String(byteData)
    val domain: Either[Error, Domain] = decode[Domain](zkData)
    domain.getOrElse(Domain("JSON parse error", "", "", "", "", 0, 0))
  }

  /** Create Libvirt Domain on free agent host
    *
    * @param d Domain case class
    * @param zk ZooKeeper client
    * @param ec ExecutionContext for Foture
    * @return Future with Domain case class with new status
    */
  def createDomain(d: Domain)(implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[Domain] = {
    activeAgents().flatMap { names =>
      names.isEmpty match {
        case true => Future.successful(d.copy(status = "Failed, no agents available"))
        case false => Future.successful(d)
      }
    }
  }

  /*
  private def getFreeAgent(names: List[AgentAlias], d: Domain)
                          (implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[Agent] = Future {
    val persisted: Future[List[Agent]] = persistedAgentsFull()

    persisted.flatMap { agents=>
      val free = agents.filter(_.)
    }
  }
  */
}

