package net.wohey.airc

sealed trait ServerIrcMessage extends IrcMessage

case class NoticeMessage(serverName: String, nick: String, text: String) extends ServerIrcMessage {
  override def toString = s":$serverName NOTICE :$nick $text"
}

case class NeedMoreParamsMessage(serverName: String, command: IrcCommand) extends ServerIrcMessage {
  override def toString = s":$serverName 461 $command :Not enough parameters"
}

case class UnknownCommandMessage(serverName: String, command: String) extends ServerIrcMessage {
  override def toString = s":$serverName 421 $command :Unknown command"
}

case class AlreadyAuthenticatedMessage(serverName: String) extends ServerIrcMessage {
  override def toString = s":$serverName NOTICE : Already authenticated"
}