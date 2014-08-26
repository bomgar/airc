package net.wohey.airc

case class UserIrcMessage(prefix: Option[MessagePrefix],
                      command: String,
                      arguments: List[String]) extends IrcMessage
