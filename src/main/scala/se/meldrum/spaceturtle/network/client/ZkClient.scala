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

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import se.meldrum.spaceturtle.utils.ZkConfig

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

object ZkClient extends ZkClient
