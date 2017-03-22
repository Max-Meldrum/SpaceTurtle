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

  def run(port: Int): Unit = {
    val bind = setupServer
      .bind(8080)

    val bindSetup = Try(
      bind.sync()
    )

    bindSetup match {
      case Success(v) => bind.channel().closeFuture().sync() // On binding success: Block until channel closes
      case Failure(e) => logger.error(e.toString)
    }
    close()
  }

  def close(): Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
