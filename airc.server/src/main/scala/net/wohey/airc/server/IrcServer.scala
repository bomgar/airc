package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Status}
import akka.io.IO
import akka.stream.io.StreamTcp
import akka.stream.io.StreamTcp.IncomingTcpConnection
import akka.stream.scaladsl.Flow
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.util.{ByteString, Timeout}
import net.wohey.airc.parser.IrcMessageParser

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class IrcServer(val serverAddress: InetSocketAddress, shutdownSystemOnError : Boolean = false) extends Actor with ActorLogging {

  implicit val system = context.system

  val settings = MaterializerSettings()

  val materializer = FlowMaterializer(settings)

  implicit val timeout = Timeout(5.seconds)

  val delimiter = ByteString("\r\n".getBytes("UTF-8"))

  override def preStart() = {
    IO(StreamTcp) ! StreamTcp.Bind(settings, serverAddress)
  }

  def receive: Receive = {
    case serverBinding: StreamTcp.TcpServerBinding =>
      log.info("Server started, listening on: " + serverBinding.localAddress)

      Flow(serverBinding.connectionStream).foreach { conn ⇒
        log.info(s"Client connected from: ${conn.remoteAddress}")
        createIncomingFlow(conn)
      }.consume(materializer)
    case Status.Failure(e)  =>
      log.error(e, s"Server could not bind to $serverAddress: ${e.getMessage}")
      if(shutdownSystemOnError) system.shutdown()
  }

  def createIncomingFlow(conn: IncomingTcpConnection) {
    val delimiterFraming = new DelimiterFraming(maxSize = 1000, delimiter = delimiter)
    Flow(conn.inputStream)
      .mapConcat(delimiterFraming.apply)
      .map(_.utf8String)
      .map(IrcMessageParser.parse)
      .filter(_.isSuccess)
      .map {
        case Success(m) => m
        case Failure(_) => throw new IllegalStateException("All failures should have been filtered already.")
      }
      .foreach(println(_))
      .consume(materializer)
  }
}
