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

package master.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import zookeeper.{Agent, ZkClient}
import zookeeper.ZkClient.{AgentAlias, ZooKeeperClient}

import scala.concurrent.{ExecutionContext, Future}

class DomainRoute()(implicit val ec: ExecutionContext, implicit val zk: ZooKeeperClient)
  extends LazyLogging {

  import master.http.JsonSupport._

  val route: Route =
    pathPrefix("domain") {
      health~
      agents
    }

  val agents: Route =
    pathPrefix("agents") {
      path("active") {
        get {
          complete(ZkClient.activeAgents())
        }
      }~
      pathPrefix("persisted") {
        path("names") {
          get {
            complete(ZkClient.persistedAgents())
          }
        }~
        path("full") {
          get {
            complete(persistedAgentsFull)
          }
        }
      }
    }


  val health: Route =
    path("health") {
      get {
        extractClientIP {ip =>
          val remoteHost = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
          logger.info("Client: " + remoteHost + " Checking health")
          complete(getHealth())
        }
      }
    }

  private def getHealth(): String = {
    ZkClient.isConnected() match {
      case true => "up"
      case false => "down"
    }
  }

  private def persistedAgentsFull: Future[List[Agent]] = {
    ZkClient.persistedAgents().flatMap { names =>
      Future.sequence(names.map(n => ZkClient.getAgent(n)))
    }
  }
}

