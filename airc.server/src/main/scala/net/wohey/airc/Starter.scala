package net.wohey.airc

import java.net.{InetSocketAddress, InetAddress}

import akka.actor.ActorSystem
import net.wohey.airc.server.IrcServer

object Starter {

  def main(args: Array[String]): Unit = {
    val ircServerSystem: ActorSystem = ActorSystem.create("ircserver")
    implicit val ec = ircServerSystem.dispatcher

    val serverStart = new IrcServer(system = ircServerSystem, new InetSocketAddress(9999)).start()

    serverStart.onFailure {
      case _ => ircServerSystem.shutdown()
    }
  }

}
