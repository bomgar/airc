package net.wohey.airc

case class MessagePrefix (nick : String,
                          user : String,
                          host : String) {
  override def toString = s":$nick!$user@$host"
}
