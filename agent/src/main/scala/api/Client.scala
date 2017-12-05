package api

import fs2.{Strategy, Task}
import zookeeper.ZkClient.ZooKeeperClient
import zookeeper.{Agent, Application, ZkClient, ZkPaths}
import io.circe.generic.auto._
import io.circe.syntax._

object Client extends ZkPaths {
  implicit val strategy = Strategy.fromExecutionContext(scala.concurrent.ExecutionContext.Implicits.global)

  def registerApp(agent: Agent, app: Application)(implicit zk: ZooKeeperClient): Task[Boolean] = Task {
    val path = agentPersistedPath + "/" + agent.host + "/" + app.name
    ZkClient.nodeExists(path) match {
      case true => false
      case false => ZkClient.createNode(path, Some(app.asJson.noSpaces))
    }
  }
}
