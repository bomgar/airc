package net.wohey.airc

import java.net.{InetSocketAddress, InetAddress}

import akka.actor.{Props, ActorSystem}
import net.wohey.airc.server.IrcServer

object Starter {

  def main(args: Array[String]): Unit = {
    val ircServerSystem = ActorSystem.create("ircserver-system")
    ircServerSystem.actorOf(Props(new IrcServer(new InetSocketAddress(9999), shutdownSystemOnError = true)), "ircserver")
  }

}
