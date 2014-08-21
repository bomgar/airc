package net.wohey.airc

import java.net.{InetSocketAddress, InetAddress}

import akka.actor.ActorSystem
import net.wohey.airc.server.IrcServer

object Starter {

  def main(args: Array[String]): Unit = {
    new IrcServer(system = ActorSystem.create("ircserver"), new InetSocketAddress(9999)).start()
  }

}
