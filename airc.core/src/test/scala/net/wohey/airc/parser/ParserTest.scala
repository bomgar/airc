package net.wohey.airc.parser

import net.wohey.airc.{MessagePrefix, IncomingIrcMessage}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}


@RunWith(classOf[JUnitRunner])
class ParserTest extends FunSuite with Matchers {

  test("recognize command") {

    val parseResult = IrcMessageParser.parse("TEST")
    parseResult.get should be(IncomingIrcMessage(prefix = None, command = "TEST", arguments = List()))
  }

  test("recognize command with prefix") {

    val parseResult = IrcMessageParser.parse(":nick!user@test.de.com TEST")
    parseResult.get should be(
      IncomingIrcMessage(prefix = Some(MessagePrefix(nick = "nick", user = "user", host = "test.de.com")), command = "TEST", arguments = List())
    )
  }

  test("recognize command with prefix and arguments") {

    val parseResult = IrcMessageParser.parse(":nick!user@test.de.com TEST bla blubb")
    parseResult.get should be(
      IncomingIrcMessage(
        prefix = Some(MessagePrefix(nick = "nick", user = "user", host = "test.de.com")),
        command = "TEST",
        arguments = List("bla", "blubb")
      )
    )
  }

  test("recognize command with prefix, arguments and trailing") {

    val parseResult = IrcMessageParser.parse(":nick!user@test.de.com TEST bla blubb :asdlkasd kalsd asdk asldk asd as")
    parseResult.get should be(
      IncomingIrcMessage(
        prefix = Some(MessagePrefix(nick = "nick", user = "user", host = "test.de.com")),
        command = "TEST",
        arguments = List("bla", "blubb", "asdlkasd kalsd asdk asldk asd as")
      )
    )
  }

  test("recognize command with arguments") {

    val parseResult = IrcMessageParser.parse("TEST bla blubb")
    parseResult.get should be(
      IncomingIrcMessage(
        prefix = None,
        command = "TEST",
        arguments = List("bla", "blubb")
      )
    )
  }

  test("recognize command with prefix, command arguments and trailing") {

    val parseResult = IrcMessageParser.parse("TEST bla blubb :asdlkasd kalsd asdk asldk asd as")
    parseResult.get should be(
      IncomingIrcMessage(
        prefix = None,
        command = "TEST",
        arguments = List("bla", "blubb", "asdlkasd kalsd asdk asldk asd as")
      )
    )
  }

  test("recognize illegal input") {
    val parseResult = IrcMessageParser.parse("@@")
    parseResult should be ('isFailure)
  }

  test("recognize illegal CR") {
    val parseResult = IrcMessageParser.parse("TEST bla blubb :asdlkasd kalsd asdk asldk asd\r as")
    parseResult should be ('isFailure)
  }

  test("recognize illegal LF in trailing") {
    val parseResult = IrcMessageParser.parse("TEST bla blubb :asdlkasd kalsd asdk asldk asd\n as")
    parseResult should be ('isFailure)
  }

  test("recognize illegal LF in args") {
    val parseResult = IrcMessageParser.parse("TEST bla\n blubb :asdlkasd kalsd asdk asldk asd as")
    parseResult should be ('isFailure)
  }

}
