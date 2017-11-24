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

import com.typesafe.scalalogging.LazyLogging
import core.{Coordinator, LeaderElection}
import utils.{Leader, Worker}
import zookeeper.{Agent, ZkClient, ZkSetup}

import scala.util.{Failure, Success}

/** Main Starting Point of Program
  *
  * Starts the agent and joins the cluster,
  * by creating a session to ZooKeeper
  */
object AgentSystem extends App with LazyLogging {
  implicit val zk = ZkClient.zkCuratorFrameWork
  private val agent = Agent("test", "node-1")
  private val leaderElection = new LeaderElection(agent)

  run()

  def run(): Unit = {
    ZkClient.connect() match {
      case true => agentSetup()
      case false => logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
    }

    // Make sure our agent's leader latch is removed
    leaderElection.closeLatch()

    // Close CuratorFramework at end
    if (ZkClient.isConnected())
      zk.close()
  }

  private def agentSetup(): Unit = {
    ZkSetup.run()
    ZkClient.joinCluster(agent) match {
      case Success(_) => {
        logger.info("ZooKeeper session is now active")
        ZkClient.registerAgent(agent)
        leaderElection.exists() match {
          case true => logger.error(s"Something went wrong, ${agent.host} is already in the leader latch")
          case false => systemExecution()
        }
      }
      case Failure(e) => logger.error("Error occurred, " + e.toString)
    }
  }

  private def systemExecution(): Unit = {
    val coordinator = new Coordinator(leaderElection)
    leaderElection.startLatch()
    leaderElection.isLeader() match {
      case true => coordinator.start(Leader)
      case false => coordinator.start(Worker)
    }
  }

}
