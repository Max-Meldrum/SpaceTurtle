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

/** ZooKeeper Config Trait
    *
    * Fetches host and port from application.conf
    */
trait ZooKeeperConfig extends Config {
  val zkHost = config.getString("zookeeper.host")
  val zkPort = config.getInt("zookeeper.port")
  val zkConnectionTimeout = config.getInt("zookeeper.connectionTimeout")
  val zkSessionTimeout = config.getInt("zookeeper.sessionTimeout")
  val zkMaxReconnections = config.getInt("zookeeper.maxReconnections")
  val zkNamespace = config.getString("zookeeper.namespace")
}
