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

package se.meldrum.spaceturtle.network.server

import com.typesafe.scalalogging.LazyLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}

import scala.util.{Failure, Success, Try}

object SpaceTurtleServer extends LazyLogging {
  private val bossGroup = new NioEventLoopGroup(1)
  private val workerGroup = new NioEventLoopGroup

  /** Netty Server Setup
    *
    * This sets up channels and handlers
    */
  val setupServer = new ServerBootstrap()
    .group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .handler(new LoggingHandler(LogLevel.INFO))
    .childHandler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        ch.pipeline
          .addLast(new SpaceTurtleServerHandler())
      }
    })

  /** Starts SpaceTurtle Server
    *
    * Tries to bind the chosen port and then starts listening for connections
    * @param port Which port the server will run on
    */
  def run(port: Int): Unit = {
    val bind = setupServer
      .bind(8080)

    val bindSetup = Try(bind.sync())

    bindSetup match {
      case Success(_) => bind.channel().closeFuture().sync() // On binding success: Block until channel closes
      case Failure(e) => logger.error(e.toString)
    }
    close()
  }

  def close(): Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
