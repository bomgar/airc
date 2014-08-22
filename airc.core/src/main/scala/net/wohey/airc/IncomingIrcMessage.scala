package net.wohey.airc

case class IncomingIrcMessage(prefix: Option[MessagePrefix],
                      command: String,
                      arguments: List[String]) extends IrcMessage
