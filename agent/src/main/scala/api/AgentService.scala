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

import com.typesafe.scalalogging.LazyLogging
import core.{Coordinator, LeaderElection}
import models.{AgentState}
import org.http4s._
import org.http4s.dsl._
import utils.{Leader, Worker}
import zookeeper.{Agent, ZkClient, ZkSetup}
import io.circe.syntax._
import org.http4s.circe._

import scala.util.{Failure, Success}


object AgentService extends Encoders with LazyLogging {
  implicit val zk = ZkClient.zkCuratorFrameWork
  private[this] val agent = Agent("test", "node-1")
  private[this] val leader = new LeaderElection(agent)
  private[this] val coordinator = new Coordinator(leader)
  private[this] val startTime = System.currentTimeMillis()

  // To handle JSON decoding..
  import io.circe.generic.auto._

  setup()

  private def setup(): Unit = {
    ZkClient.connect() match {
      case true =>
        registration()
      case false =>
        logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
        shutdown
    }

    def registration(): Unit = {
      ZkSetup.run()
      ZkClient.joinCluster(agent) match {
        case Success(_) => {
          logger.info("ZooKeeper session is now active")
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

  private def shutdown = System.exit(0)


  val main = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
    case GET -> Root / "status" =>
      val t = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
      val uptime = t + " seconds"
      coordinator.getState() match {
        case Leader => Ok(AgentState(uptime, "Leader").asJson)
        case Worker => Ok(AgentState(uptime, "Worker").asJson)
      }
  }

}

