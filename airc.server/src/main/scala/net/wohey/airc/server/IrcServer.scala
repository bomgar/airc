package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import akka.stream.io.StreamTcp
import akka.stream.scaladsl.Flow
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._

class IrcServer(system: ActorSystem, val serverAddress: InetSocketAddress) extends SLF4JLogging {

  implicit val sys = system

  implicit val executionContext = system.dispatcher

  val settings = MaterializerSettings()

  val materializer = FlowMaterializer(settings)

  implicit val timeout = Timeout(5.seconds)

  def start() : Unit = {
    val serverFuture = IO(StreamTcp) ? StreamTcp.Bind(settings, serverAddress)

    serverFuture.onSuccess {
      case serverBinding: StreamTcp.TcpServerBinding =>
        log.info("Server started, listening on: " + serverBinding.localAddress)

        Flow(serverBinding.connectionStream).foreach { conn ⇒
          log.info(s"Client connected from: ${conn.remoteAddress}")
          conn.inputStream.subscribe(conn.outputStream)
        }.consume(materializer)
    }

    serverFuture.onFailure {
      case e: Throwable =>
        log.error(s"Server could not bind to $serverAddress: ${e.getMessage}")
        sys.shutdown()
    }

  }


}
