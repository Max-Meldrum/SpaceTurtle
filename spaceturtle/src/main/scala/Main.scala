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

package spaceturtle

import com.typesafe.scalalogging.LazyLogging
import network.client.ZkClient
import network.server.{SpaceTurtleServer, ZkSetup}
import utils.SpaceTurtleConfig

import scala.util.{Failure, Success}


/** Main Starting Point of Program
  *
  * Starts the SpaceTurtle server and joins the cluster,
  * by creating a session to ZooKeeper
  */
object Main extends App with LazyLogging with SpaceTurtleConfig {

  implicit val zk = ZkClient.zkCuratorFrameWork
  ZkClient.connect()

  // To let it try to connect before checking connection status
  Thread.sleep(500)

  val connected = ZkClient.isConnected()

  connected match {
    case true => {
      ZkSetup.run() // Create needed Znodes if they don't exist
      ZkClient.joinCluster(spaceTurtleHost, spaceTurtleUser, spaceTurtlePort) match {
        case Success(_) => {
          logger.info("ZooKeeper session is now active")
          SpaceTurtleServer.run(spaceTurtlePort)
        }
        case Failure(e) => logger.error("Error occured, " + e.toString)
      }

    }
    case false => {
      logger.error("Failed to establish initial connection to ZooKeeper, shutting down")
    }
  }

  zk.close()
}
