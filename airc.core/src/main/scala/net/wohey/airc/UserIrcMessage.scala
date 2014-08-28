package net.wohey.airc

case class UserIrcMessage(prefix: Option[MessagePrefix],
                      command: String,
                      arguments: List[String]) extends IrcMessage {
  //TODO add : for trailing or add a separate field just for that
  override def toString = prefix.map(_.toString + " ").getOrElse("") + command + arguments.mkString(" ")
}
