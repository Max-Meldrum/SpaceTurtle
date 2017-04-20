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
import com.typesafe.scalalogging.LazyLogging
import zookeeper.{Agent, ZkClient}
import zookeeper.ZkClient.ZooKeeperClient

import scala.concurrent.{ExecutionContext, Future}

class DomainRoute()(implicit val ec: ExecutionContext, implicit val zk: ZooKeeperClient)
  extends LazyLogging {

  import master.http.JsonSupport._

  // TODO: Refactor into something more pretty
  val route =
    pathPrefix("domain") {
      path("health") {
        get {
          extractClientIP {ip =>
            val remoteHost = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
            logger.info("Client: " + remoteHost + " Checking health")
            complete(health())
          }
        }
      }~
      pathPrefix("agents") {
        path("active") {
          get {
            complete(getActiveAgents())
          }
        }~
        get {
          complete("all nodes")
        }
      }
    }

  private def health(): String = {
    ZkClient.isConnected() match {
      case true => "up"
      case false => "down"
    }
  }

  private def getActiveAgents(): Future[List[Agent]] =
    Future(ZkClient.getAgentNames().map(Agent(_)))

}
