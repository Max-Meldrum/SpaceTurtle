package spaceturtle.network.client

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import spaceturtle.utils.FileContainer


class SpaceTurtleFileHandler(f: FileContainer) extends SimpleChannelInboundHandler[FileContainer] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: FileContainer): Unit = {

  }

}
