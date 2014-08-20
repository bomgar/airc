package net.wohey.airc.parser

import net.wohey.airc.{MessagePrefix, IrcMessage}

import scala.util.Try
import scala.util.parsing.combinator._

object IrcMessageParser extends RegexParsers {

  override protected val whiteSpace = """[ ]+""".r

  val nick: Parser[String] = """[a-zA-Z0-9-\[\]\\`\^\{\}]+""".r

  val user: Parser[String] = """[^\s@]+""".r

  val host: Parser[String] = """[^\s@]+""".r

  val arg: Parser[String] = """[^:\s][\S]+""".r

  val trailing: Parser[String] = ":" ~ """[^\r\n]*""".r ^^ { case _ ~ s => s}

  val prefix: Parser[MessagePrefix] = ":" ~ nick ~ "!" ~ user ~ """@""" ~ host ^^ {
    case _ ~ n ~ _ ~ u ~ _ ~ h => new MessagePrefix(nick = n, user = u, host = h)
  }

  val command: Parser[String] = """[a-zA-Z]+""".r

  val message = prefix.? ~ command ~ arg.* ~ trailing.? ^^ {
    case p ~ c ~ a ~ t =>
      val arguments = a ::: t.map(List(_)).getOrElse(List.empty)
      IrcMessage(prefix = p, command = c, arguments = arguments)
  }

  def parse(text: String): Try[IrcMessage] = parseAll(message, text) match {
    case Success(ircMessage, _) => scala.util.Success(ircMessage)
    case NoSuccess(msg, _)      => scala.util.Failure(new IllegalArgumentException(msg))
  }

}

