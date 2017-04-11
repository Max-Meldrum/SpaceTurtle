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

package se.meldrum.spaceturtle

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import se.meldrum.spaceturtle.network.client.ZkClient
import se.meldrum.spaceturtle.network.server.{SpaceTurtleServer, ZkSetup}
import se.meldrum.spaceturtle.utils.SpaceTurtleConfig


/** Main Starting Point of Program
  *
  * Starts the SpaceTurtle server on port 8080 by default,
  * or by the port sent in by command line.
  */
object Main extends App with LazyLogging with SpaceTurtleConfig {
  var port = None: Option[Int]
  val default = ConfigFactory.load()

  val conf = {
    if (args.length > 1) {
      args(0) match {
        case "--conf" => loadConfig(args(1)).getOrElse(default)
      }
    } else {
      default
    }
  }

  def loadConfig(path: String): Option[Config] = {
    val file = new File(path)
    file.exists() match {
      case true => Some(ConfigFactory.parseFile(file))
      case false => None
    }
  }

  implicit val zk = ZkClient.zkCuratorFrameWork
  ZkClient.connect()

  // To let it try to connect before checking connection status
  Thread.sleep(300)

  val connected = ZkClient.isConnected()

  connected match {
    case true => {
      ZkSetup.run() // Create needed Znodes if they don't exist
      ZkClient.joinCluster(spaceTurtleHost, spaceTurtleUser, spaceTurtlePort)
      logger.info("ZooKeeper session is now active")
      SpaceTurtleServer.run(port.getOrElse(spaceTurtlePort))
    }
    case false => logger.info("Failed to establish initial connection to ZooKeeper, shutting down")
  }

}
