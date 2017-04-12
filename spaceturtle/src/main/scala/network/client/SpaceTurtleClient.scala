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

package spaceturtle.network.client

import java.net.InetSocketAddress

import com.typesafe.scalalogging.LazyLogging
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup

import scala.util.{Failure, Success}
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestEncoder, HttpResponseDecoder}
import io.netty.handler.stream.ChunkedWriteHandler
import spaceturtle.utils.{Agent, FileContainer}

import scala.util.Try

// $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
class SpaceTurtleClient(agent: Agent) extends LazyLogging {

  def run(msg: String): Unit = {
    val group = new NioEventLoopGroup()
    val setup = Try {
      val b = new Bootstrap()
      b.group(group)
        .channel(classOf[NioSocketChannel])
        .remoteAddress(new InetSocketAddress(agent.hostName, agent.port))
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            ch.pipeline().addLast(new SpaceTurtleClientHandler(msg))
          }
        })

      val future = b.connect().sync() // Blocks until channel closes

      group.shutdownGracefully().sync() // Shutdown thread pool and release resources
    }

    setup match {
      case Success(_) =>
      case Failure(e) => logger.error(e.toString)
    }
  }

  def transferFile(f: FileContainer): Unit = {
    val group = new NioEventLoopGroup()
    val setup = Try {
      val b = new Bootstrap()
      b.group(group)
        .channel(classOf[NioSocketChannel])
        .remoteAddress(new InetSocketAddress(agent.hostName, agent.port))
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            ch.pipeline().addLast(new HttpResponseDecoder())
            ch.pipeline().addLast(new HttpObjectAggregator(60000))
            ch.pipeline().addLast(new HttpRequestEncoder())
            ch.pipeline().addLast(new ChunkedWriteHandler())
            ch.pipeline().addLast(new SpaceTurtleFileHandler(f))
          }
        })

      val future = b.connect().sync() // Blocks until channel closes

      group.shutdownGracefully().sync() // Shutdown thread pool and release resources
    }

    setup match {
      case Success(_) =>
      case Failure(e) => logger.error(e.toString)
    }

  }
}
