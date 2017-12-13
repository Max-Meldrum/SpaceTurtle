package api

import zookeeper.ZkClient.ZooKeeperClient
import zookeeper.{Agent, Application, ZkClient, ZkPaths}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

object AgentClient extends ZkPaths {

  //Exotic
  def registerApp(agent: Agent, app: Application)
                 (implicit zk: ZooKeeperClient, ec: ExecutionContext): Future[Boolean] = Future {
    val path = agentPersistedPath + "/" + agent.host + "/" + app.name
    ZkClient.nodeExists(path) match {
      case true => false
      case false => ZkClient.createNode(path, Some(app.asJson.noSpaces))
    }
  }
}
