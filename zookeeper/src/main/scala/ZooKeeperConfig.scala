package zookeeper

import com.typesafe.config.ConfigFactory

/** ZooKeeper Config Trait
    *
    * Fetches host and port from application.conf
    */
trait ZooKeeperConfig {
  val zkConfig = ConfigFactory.load()
  val zkHost = zkConfig.getString("zookeeper.host")
  val zkPort = zkConfig.getInt("zookeeper.port")
  val zkConnectionTimeout = zkConfig.getInt("zookeeper.connectionTimeout")
  val zkSessionTimeout = zkConfig.getInt("zookeeper.sessionTimeout")
  val zkMaxReconnections = zkConfig.getInt("zookeeper.maxReconnections")
  val zkNamespace = zkConfig.getString("zookeeper.namespace")
}
