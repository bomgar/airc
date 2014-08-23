package net.wohey.airc

sealed trait ServerIrcMessage extends IrcMessage

case class NoticeMessage(serverName : String, nick : String) extends ServerIrcMessage {
  override def toString = s":$serverName NOTICE :$nick WELCOME TO OUR SIRC SERVER! ENJOY!"
}
