package zookeeper

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.api.ACLProvider
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.ZooDefs
import org.apache.zookeeper.data.ACL


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

object ZkClient extends ZkClient {

}

