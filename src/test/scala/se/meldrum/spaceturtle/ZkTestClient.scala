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

package se.meldrum.spaceturtle

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.apache.zookeeper.data.Stat
import se.meldrum.spaceturtle.utils.ZkConfig

/** ZooKeeper Test Client
  *
  * Is used when performing unit tests
  */
trait ZkTestClient extends ZkConfig {
  val testServer = new TestingServer()
  testServer.start()
  val zkRetryPolicy = new ExponentialBackoffRetry(1000, zkMaxReconnections)
  val zkCuratorFrameWork = CuratorFrameworkFactory.builder()
    .namespace(zkNamespace)
    .connectString(testServer.getConnectString)
    .retryPolicy(zkRetryPolicy)
    .sessionTimeoutMs(zkConnectionTimeout)
    .connectionTimeoutMs(zkSessionTimeout)
    .build()

  zkCuratorFrameWork.start()
}

object ZkTestClient extends ZkTestClient {

  /** Check if Znode path exists
    *
    * @param stat On null, it means we could not find the path
    * @return True if it exist, otherwise false
    */
  def pathExists(stat: Stat): Boolean = {
    stat match {
      case null => false
      case _ => true
    }
  }

  def cleanZnodes(): Unit = {
    // Agent node
    zkCuratorFrameWork.delete().deletingChildrenIfNeeded().forPath("/agents")
  }

  def nodeSetup(): Unit = {
    // Agent node
    zkCuratorFrameWork.create().forPath("/agents")
  }

}
