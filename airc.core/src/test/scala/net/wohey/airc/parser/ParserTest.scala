package net.wohey.airc.parser

import net.wohey.airc.{MessagePrefix, UserIrcMessage}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{WordSpecLike, Inside, Matchers}


@RunWith(classOf[JUnitRunner])
class ParserTest extends WordSpecLike with Matchers with Inside {

  "An irc message parser" should {

    "recognize command" in {
      whenParsingIrcMessage("TEST") { case UserIrcMessage(prefix, command, arguments) =>
        prefix should be(None)
        command should be("TEST")
        arguments should be(List.empty)
      }
    }

    "recognize command with prefix" in {
      whenParsingIrcMessage(":nick!user@test.de.com TEST") { case UserIrcMessage(prefix, command, arguments) =>
        inside(prefix) { case Some(MessagePrefix(nick, user, host)) =>
          nick should be("nick")
          user should be("user")
          host should be("test.de.com")
        }
        command should be("TEST")
        arguments should be(List.empty)
      }
    }

    "recognize command with prefix and arguments" in {
      whenParsingIrcMessage(":nick!user@test.de.com TEST bla blubb") { case UserIrcMessage(prefix, command, arguments) =>
        inside(prefix) { case Some(MessagePrefix(nick, user, host)) =>
          nick should be("nick")
          user should be("user")
          host should be("test.de.com")
        }
        command should be("TEST")
        arguments should be(List("bla", "blubb"))
      }
    }

    "recognize command with prefix, arguments and trailing" in {

      whenParsingIrcMessage(":nick!user@test.de.com TEST bla blubb :asdlkasd kalsd asdk asldk asd as") { case UserIrcMessage(prefix, command, arguments) =>
        inside(prefix) { case Some(MessagePrefix(nick, user, host)) =>
          nick should be("nick")
          user should be("user")
          host should be("test.de.com")
        }
        command should be("TEST")
        arguments should be(List("bla", "blubb", "asdlkasd kalsd asdk asldk asd as"))
      }
    }

    "recognize command with arguments" in {

      whenParsingIrcMessage("TEST bla blubb") { case UserIrcMessage(prefix, command, arguments) =>
        prefix should be(None)
        command should be("TEST")
        arguments should be(List("bla", "blubb"))
      }
    }

    "recognize command with prefix, command arguments and trailing" in {
      whenParsingIrcMessage("TEST bla blubb :asdlkasd kalsd asdk asldk asd as") { case UserIrcMessage(prefix, command, arguments) =>
        prefix should be(None)
        command should be("TEST")
        arguments should be(List("bla", "blubb", "asdlkasd kalsd asdk asldk asd as"))
      }
    }

    "recognize illegal input" in {
      val parseResult = IrcMessageParser.parse("@@")
      parseResult should be('isFailure)
    }

    "recognize illegal CR" in {
      val parseResult = IrcMessageParser.parse("TEST bla blubb :asdlkasd kalsd asdk asldk asd\r as")
      parseResult should be('isFailure)
    }

    "recognize illegal LF in trailing" in {
      val parseResult = IrcMessageParser.parse("TEST bla blubb :asdlkasd kalsd asdk asldk asd\n as")
      parseResult should be('isFailure)
    }

    "recognize illegal LF in args" in {
      val parseResult = IrcMessageParser.parse("TEST bla\n blubb :asdlkasd kalsd asdk asldk asd as")
      parseResult should be('isFailure)
    }
  }

  private def whenParsingIrcMessage(ircMessage: String)(pf: PartialFunction[UserIrcMessage, Unit]) = {
    val parseResult = IrcMessageParser.parse(ircMessage)
    inside(parseResult.get)(pf)

  }

}
