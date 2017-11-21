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
import zookeeper.ZkClient
import zookeeper.ZkClient.ZooKeeperClient
import scala.concurrent.ExecutionContext


class HealthRoute()(implicit val ec: ExecutionContext, implicit val zk: ZooKeeperClient)
  extends LazyLogging {

  val route: Route =
    pathPrefix("health") {
      zookeeper
    }

  private[this] val zookeeper: Route =
    path("zookeeper") {
      get {
        extractClientIP { ip =>
          val remoteHost = ip.toOption
            .map(_.getHostAddress)
            .getOrElse("unknown")
          logger.info("Client: " + remoteHost + " Checking health of zookeeper")
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
}
