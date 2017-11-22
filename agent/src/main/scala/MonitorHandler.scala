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
import zookeeper.ZkClient.ZooKeeperClient
import zookeeper.{Agent, ZkPaths}


class MonitorHandler(agent: Agent)(implicit zk: ZooKeeperClient)
  extends ZkPaths with LazyLogging {

  private[this] val path = agentPersistedPath + "/" + agent.host
  private[this] val monitorPath = path  + "/" + "applications"
  private[this] val cache = new PathChildrenCache(zk, monitorPath, true)

  /** Curator recipe that will keep track of specified path and notify us
    * of the changes made to it.
    */
  def createCache(): Unit = {
    cache.start()
    addCacheListener(cache)
  }

  /** Add listener to PathChildrenCache
    *
    * @param pathCache target cache
    */
  private def addCacheListener(pathCache: PathChildrenCache): Unit = {
    val listener = new PathChildrenCacheListener() {
      override def childEvent(client: ZooKeeperClient, event: PathChildrenCacheEvent): Unit = {
        event.getType match {
          case Type.CHILD_ADDED => logger.info("znode added")
          case Type.CHILD_UPDATED => logger.info("znode updated")
          case Type.CHILD_REMOVED => logger.info("znode removed")
          case Type.CONNECTION_LOST => logger.info("agentCache listener lost connection")
          case _ => logger.info("unknown")
        }
      }
    }
    pathCache.getListenable.addListener(listener)
  }

  /** Fetch Monitor cache
    *
    * @return  PathChildrenCache
    */
  def getMonitorCache(): PathChildrenCache = cache
}
