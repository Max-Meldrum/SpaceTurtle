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

import java.nio.channels.SocketChannel

import com.typesafe.scalalogging.LazyLogging
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.util.ReferenceCountUtil

import scala.util.{Failure, Success, Try}

// $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
class SpaceTurtleServerHandler extends SimpleChannelInboundHandler[SocketChannel] with LazyLogging {

  override def channelRead0(ctx: ChannelHandlerContext, msg: SocketChannel) =  ???

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val buf = msg.asInstanceOf[ByteBuf]

    val received = Try {
      val data = buf.toString(io.netty.util.CharsetUtil.US_ASCII)
      logger.debug("Msg received: " + data)
    }

    received match {
      case Success(s) => ReferenceCountUtil.release(msg)
      case Failure(e) => logger.debug(e.toString)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.debug(cause.toString)
    ctx.close()
  }

}
