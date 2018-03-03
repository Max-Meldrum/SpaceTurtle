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
  * Sets up the REST server
  */
object AgentSystem extends App with LazyLogging with AgentConfig {
  implicit val zk = ZkClient.zkCuratorFrameWork
  // Test agent for now..
  private[this] val agent = Agent("test", "node-1")
  private[this] val election = new LeaderElection(agent)
  private[this] val coordinator = new Coordinator(election)

  // Initialize SpaceTurtle Agent
  setup()

  private def setup(): Unit = {
    if (ZkClient.connect()) {
      if (register()) {
        implicit val system = ActorSystem("Agent")
        implicit val ec = system.dispatcher
        implicit val materializer = ActorMaterializer()
        val service = new AgentService(coordinator, agent)
        logger.info("Setting up SpaceTurtle Agent at " + host + ":" + port)
        Http().bindAndHandle(service.route, host , port)
      }
    } else {
      logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
    }
  }

  private def register(): Boolean = {
    ZkSetup.run()
    ZkClient.joinCluster(agent) match {
      case Success(_) => {
        logger.info("ZooKeeper session is now active")
        ZkClient.registerAgent(agent)
        if (election.agentExists()) {
          logger.error(s"Something went wrong, ${agent.host} is already in the leader latch")
          false
        } else {
          election.startLatch()
          if (election.isLeader())
            coordinator.start(Leader)
          else
            coordinator.start(Worker)

          true
        }
      }
      case Failure(e) =>
        logger.error("Error occurred, " + e.toString)
        false
    }
  }
}
