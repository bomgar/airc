package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, Actor}
import net.wohey.airc.IncomingIrcMessage
import net.wohey.airc.server.Connection.IncomingFlowClosed

class Connection(remoteAddress: InetSocketAddress) extends Actor with ActorLogging {

  def receive: Receive = {
    case IncomingFlowClosed =>
      log.info(s"$remoteAddress closed connection")
      context.stop(self)
    case message : IncomingIrcMessage =>
      log.debug("{}: {}", remoteAddress, message)
  }

}

object Connection {
  case class IncomingFlowClosed()
}
