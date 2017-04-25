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
import org.libvirt.Connect
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import zookeeper.ZkClient.ZooKeeperClient
import zookeeper.ZkPaths

class AgentHandler(connect: Connect)(implicit zk: ZooKeeperClient)
  extends ZkPaths with LazyLogging {

  private[this] val path = agentPersistedPath + "/" + connect.getHostName
  private[this] val cache = new PathChildrenCache(zk, path , true)


  /** Curator recipe that will keep track of specified path and notify us
    * of the changes made to it.
    */
  def createAgentCache(): Unit = {
    cache.start()
    addAgentCacheListener(cache)
  }

  /** Add listener to PathChildrenCache
    *
    * @param pathCache target cache
    */
  private def addAgentCacheListener(pathCache: PathChildrenCache): Unit = {
    val listener = new PathChildrenCacheListener() {
      override def childEvent(client: ZooKeeperClient, event: PathChildrenCacheEvent): Unit = {
        event.getType match {
          case Type.CHILD_ADDED => logger.info("znode created")
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
  def getCache(): PathChildrenCache = cache
}
