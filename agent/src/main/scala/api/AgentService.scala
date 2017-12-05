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

import com.typesafe.scalalogging.LazyLogging
import core.{Coordinator, LeaderElection}
import org.http4s._
import org.http4s.dsl._
import utils.{Leader, Worker}
import zookeeper.{Agent, ZkClient, ZkSetup}

import scala.util.{Failure, Success}


object AgentService extends LazyLogging {
  implicit val zk = ZkClient.zkCuratorFrameWork
  private val agent = Agent("test", "node-1")
  private val leader = new LeaderElection(agent)
  private[this] val coordinator = new Coordinator(leader)

  setup()

  private def setup(): Unit = {

    ZkClient.connect() match {
      case true => registration()
      case false =>
        logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
        shutdown
    }

    def registration(): Unit = {
      ZkSetup.run()
      ZkClient.joinCluster(agent) match {
        case Success(_) => {
          logger.info("ZooKeeper session is now active")
          println("now active")
          ZkClient.registerAgent(agent)
          leader.exists() match {
            case true =>
              logger.error(s"Something went wrong, ${agent.host} is already in the leader latch")
              shutdown
            case false =>
              leader.startLatch()
              leader.isLeader() match {
                case true => coordinator.start(Leader)
                case false => coordinator.start(Worker)
              }
          }
        }
        case Failure(e) =>
          logger.error("Error occurred, " + e.toString)
          shutdown
      }
    }
  }

  // Just beautiful..
  // TODO: fix
  private def shutdown = System.exit(1)


  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
    case GET -> Root / "status" =>
      coordinator.getState() match {
        case Leader => Ok("Leader")
        case Worker => Ok("Worker")
      }
  }

}

