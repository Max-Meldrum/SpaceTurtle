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

import java.nio.file.{Files, Paths}

import se.meldrum.spaceturtle.network.client.ZkClient
import se.meldrum.spaceturtle.network.client.ZkClient.ZooKeeperClient

import scala.io.{Source, StdIn}
import scala.util.{Failure, Success, Try}


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
    * @param zk CuratorFramework client we use to start connection with
    * @return true on success, false otherwise
    */
  def connectionEstablished()(implicit zk: ZooKeeperClient): Boolean = {
    ZkClient.connect()
    Thread.sleep(300) // Let it try to connect
    ZkClient.isConnected()
  }

  /** Handles the commands sent in by the user
    *
    * @param zk Implicit CuratorFramework we pass by to other methods that use the client
    */
  def handleInput()(implicit zk: ZooKeeperClient): Unit = {
    var cmd = Array[String]()
    var serving = true

    while (serving && {cmd = StdIn.readLine("SpaceTurtle console: ").split(" "); cmd != null}) {
      cmd match {
        case Array("list", "agents") => listAgents()
        case Array("send", "msg", msg: String) => println(sendMessage(msg))
        case Array("send", "file", file: String) => sendFile(file)
        case Array("help") => println(getUsage())
        case Array("exit") => serving = false
        case _ => println("SpaceTurtle: Cannot recognize command, see help")
      }
    }
  }

  /** Fetches active agents by name
    *
    * @param zk ZooKeeper client
    */
  def listAgents()(implicit zk: ZooKeeperClient): Unit = {
    ZkClient.isConnected() match {
      case true => ZkClient.getAgentNames().foreach(println(_))
      case false => println("Could not list agents because of connection failure")
    }
  }

  /** Send Custom Message to everyone
    *
    * @param msg string containing message
    * @param zk ZooKeeper client
    */
  def sendMessage(msg: String)(implicit zk: ZooKeeperClient): String =
    ZkClient.announceClusterMessage(msg)


  /** SpaceTurtleCli Command Help
    *
    * @return returns list of commands that the user can perform
    */
  def getUsage(): String = {
    "<Available commands>\n" +
      "list agents\n" +
      "send msg <data>\n" +
      "exit\n" +
      "help"
  }

  def sendFile(path: String)(implicit zk: ZooKeeperClient): Unit = {
    val fileRead = Try(Files.readAllBytes(Paths.get(path)))

    fileRead match {
      case Success(file) => {
        println(file.length)
        ZkClient.sendFile(file)
        println("Could read")
      }
      case Failure(e) => println(e.toString)
    }
  }

}
