package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import net.wohey.airc.{NeedMoreParamsMessage, IncomingIrcMessage}
import net.wohey.airc.user.User

class IncomingMessageHandler(connection : ActorRef, user : ActorRef, remoteAddress: InetSocketAddress, serverName : String) extends SLF4JLogging {

  implicit val sender = connection

  def handleIncomingIrcMessage(message : IncomingIrcMessage) = {
    log.debug(s"$remoteAddress: $message")
    message.command match {
      case "PASS" =>
        message.arguments match {
          case List(password) => user ! User.Authenticate(password = password)
          case _              => connection ! NeedMoreParamsMessage(serverName, message.command)
        }

    }
  }

}
