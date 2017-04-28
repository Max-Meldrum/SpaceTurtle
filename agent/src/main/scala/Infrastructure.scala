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

package agent

import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.libvirt.Connect
import io.circe.generic.auto._
import io.circe.syntax._
import zookeeper.ZkClient.ZooKeeperClient
import zookeeper.{ZkClient, ZkPaths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Infrastructure(connect: Connect)(implicit zk: ZooKeeperClient)
  extends ZkPaths with LazyLogging {

  private[this] val path = agentPersistedPath + "/" + connect.getHostName + "/infrastructure"
  private[this] val domainPath = path  + "/" + "domain"
  private[this] val domain = new PathChildrenCache(zk, domainPath, true)

  /** Curator recipe that will keep track of specified path and notify us
    * of the changes made to it.
    */
  def createCache(): Unit = {
    domain.start()
    addDomainListener(domain)
  }

  /** Add listener to PathChildrenCache
    *
    * @param pathCache target cache
    */
  private def addDomainListener(pathCache: PathChildrenCache): Unit = {
    val listener = new PathChildrenCacheListener() {
      override def childEvent(client: ZooKeeperClient, event: PathChildrenCacheEvent): Unit = {
        event.getType match {
          case Type.CHILD_ADDED => newDomain(event)
          case Type.CHILD_UPDATED => logger.info("znode updated")
          case Type.CHILD_REMOVED => logger.info("znode removed")
          case Type.CONNECTION_LOST => logger.info("agentCache listener lost connection")
          case _ => logger.info("unknown")
        }
      }
    }
    pathCache.getListenable.addListener(listener)
  }

  /** Fetch cache for things like unit tests
    *
    * @return  PathChildrenCache
    */
  def getDomainCache(): PathChildrenCache = domain

  /** Handle New Domain
    *
    * @param event event that happened
    */
  private def newDomain(event: PathChildrenCacheEvent): Unit = {
    val path = event.getData.getPath
    if (path.contains("domain")) {
      ZkClient.getDomain(path).onComplete({
        case Success(d) => {
          val newDomain = d.copy(status = "processing")
          ZkClient.updateNode(path, Some(newDomain.asJson.noSpaces))
        }
        case Failure(e) => logger.error(e.toString)
      })
    }
  }
}
