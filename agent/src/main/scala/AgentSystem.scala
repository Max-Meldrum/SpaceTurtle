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

package agent

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import api.AgentService
import com.typesafe.scalalogging.LazyLogging
import core.{Coordinator, LeaderElection}
import utils.{AgentConfig, Leader, Worker}
import zookeeper.{Agent, ZkClient, ZkSetup}

import scala.util.{Failure, Success}


/** Main Starting Point of Program
  * Sets upp the REST server
  */
object AgentSystem extends App with LazyLogging with AgentConfig {
  implicit val zk = ZkClient.zkCuratorFrameWork
  private[this] val agent = Agent("test", "node-1")
  private[this] val leader = new LeaderElection(agent)
  private[this] val coordinator = new Coordinator(leader)

  val service = setup()

  private def setup(): Option[AgentService] = {
    ZkClient.connect() match {
      case true =>
        if(register()) {
          implicit val system = ActorSystem("Agent")
          implicit val ec = system.dispatcher
          implicit val materializer = ActorMaterializer()
          val service = new AgentService(coordinator, agent)
          logger.info("Setting up SpaceTurtle Agent at " + host + ":" + port)
          Http().bindAndHandle(service.route, host , port)
          Some(service)
        } else {
          None
        }
      case false =>
        logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
        None
    }
  }

  //TODO: Make more pretty...
  private def register(): Boolean = {
    ZkSetup.run()
    ZkClient.joinCluster(agent) match {
      case Success(_) => {
        logger.info("ZooKeeper session is now active")
        ZkClient.registerAgent(agent)
        leader.exists() match {
          case true =>
            logger.error(s"Something went wrong, ${agent.host} is already in the leader latch")
            false
          case false =>
            leader.startLatch()
            leader.isLeader() match {
              case true => coordinator.start(Leader)
              case false => coordinator.start(Worker)
            }
            true
        }
      }
      case Failure(e) =>
        logger.error("Error occurred, " + e.toString)
        false
    }
  }
}
