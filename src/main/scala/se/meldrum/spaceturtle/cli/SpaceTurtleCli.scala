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

package se.meldrum.spaceturtle.cli

import se.meldrum.spaceturtle.network.client.ZkClient
import scala.io.StdIn


/** SpaceTurtle Command Line Client
  *
  * A way of talking/commanding the rest of the cluster
  */
object SpaceTurtleCli extends App {

  parseCommands(args)

  // TODO: Improve
  def parseCommands(args: Array[String]): Unit = {
    implicit val client = ZkClient.zkCuratorFrameWork
    var cmd = Array[String]()
    var serving = true

    while (serving && {cmd = StdIn.readLine("SpaceTurtle console: ").split(" "); cmd != null}) {
      cmd match {
        case Array("list", "agents") => {
          ZkClient.connect()
          Thread.sleep(1000) // Let it try to connect
          val connected = client.getZookeeperClient.isConnected
          connected match {
            case true => ZkClient.getAgents().foreach(println(_))
            case false => println("Failed to connect")
          }
        }
        case Array("help") => println(getUsage())
        case Array("exit") => serving = false
        case _ => println("SpaceTurtle: Cannot recognize command, see help")
      }

    }
    // Close down Curator client when we are finished
    client.close()
  }

  def getUsage(): String = {
    "Available commands: " +
      "\n" +
      "list agents" +
      "exit"
  }
}
