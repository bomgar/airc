package net.wohey.airc

sealed trait IrcCommand

object IrcCommand{

  case object PASS extends IrcCommand
  case object NICK extends IrcCommand
  case object QUIT extends IrcCommand

  def apply(command:String): Option[IrcCommand] = {
    command match {
      case "PASS" => Some(PASS)
      case "NICK" => Some(NICK)
      case "QUIT" => Some(QUIT)
      case _      => None
    }

  }
}
