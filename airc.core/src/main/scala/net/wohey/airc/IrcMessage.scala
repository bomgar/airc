package net.wohey.airc

case class IrcMessage(prefix: Option[MessagePrefix],
                      command: String,
                      arguments: List[String])
