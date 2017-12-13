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

package api


import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives.pathPrefix
import com.typesafe.scalalogging.LazyLogging
import core.{Coordinator}
import utils.{AgentConfig, Leader, Worker}
import zookeeper.{Agent, Application}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import models.{AgentState, GeneralResponse}
import zookeeper.ZkClient.ZooKeeperClient

import scala.concurrent.{ExecutionContext, Future}


class AgentService(coordinator: Coordinator, agent: Agent)
                  (implicit ec: ExecutionContext, zk: ZooKeeperClient)
  extends LazyLogging with AgentConfig with Endpoints {
  private[this] val startTime = System.currentTimeMillis()

  // JSON marshalling/unmarshalling
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._


  val route: Route =
    pathPrefix("api") {
      pathPrefix(version) {
        statusRoute~
        registerRoute
      }
    }

  private[this] val statusRoute =
    pathPrefix(statusEndpoint) {
      get {
        val t = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
        val uptime = t + " seconds"
        coordinator.getState() match {
          case Leader => complete(AgentState(uptime, "Leader", version))
          case Worker => complete(AgentState(uptime, "Worker", version))
        }
      }
    }

  private[this] val registerRoute =
    pathPrefix(registerEndpoint) {
      post {
        entity(as[Application]) { app =>
          complete(AgentClient.registerApp(agent, app)
            .flatMap {{
              case true => Future(GeneralResponse("Created"))
              case false => Future(GeneralResponse("Already exists"))
            }})
        }
      }
    }
}

