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

import org.apache.curator.framework.CuratorFramework
import se.meldrum.spaceturtle.network.client.ZkClient
import scala.io.StdIn


/** SpaceTurtle Command Line Client
  *
  * A way of talking/commanding the rest of the cluster
  */
object SpaceTurtleCli extends App {

  parseCommands(args)

  def parseCommands(args: Array[String]): Unit = {
    implicit val client = ZkClient.zkCuratorFrameWork

    connectionEstablished match {
      case true => handleInput()
      case false => println("Failed to establish connection to ZooKeeper")
    }

    // Close down Curator client when we are finished
    client.close()
  }

  /** Starts client and checks ZooKeeper connection
    *
    * @param zkClient CuratorFramework client we use to start connection with
    * @return true on success, false otherwise
    */
  def connectionEstablished()(implicit zkClient: CuratorFramework): Boolean = {
    ZkClient.connect()
    Thread.sleep(1000) // Let it try to connect
    ZkClient.isConnected()
  }

  /** Handles the commands sent in by the user
    *
    * @param zkClient Implicit CuratorFramework we pass by to other methods that use the client
    */
  def handleInput()(implicit zkClient: CuratorFramework): Unit = {
    var cmd = Array[String]()
    var serving = true

    while (serving && {cmd = StdIn.readLine("SpaceTurtle console: ").split(" "); cmd != null}) {
      cmd match {
        case Array("list", "agents") => listAgents()
        case Array("help") => println(getUsage())
        case Array("exit") => serving = false
        case _ => println("SpaceTurtle: Cannot recognize command, see help")
      }
    }
  }

  /** Fetches active agents by name
    *
    * @param zkClient CuratorFramework client we use to connect with ZooKeeper
    */
  def listAgents()(implicit zkClient: CuratorFramework): Unit = {
    ZkClient.isConnected() match {
      case true => ZkClient.getAgents().foreach(println(_))
      case false => println("Could not list agents because of connection failure")
    }
  }

  /** SpaceTurtleCli Command Help
    *
    * @return returns list of commands that the user can perform
    */
  def getUsage(): String = {
    "<Available commands>\n" +
      "list agents\n" +
      "exit\n" +
      "help"
  }



}
