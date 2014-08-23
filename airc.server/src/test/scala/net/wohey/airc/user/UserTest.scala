package net.wohey.airc.user

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  val user = TestFSMRef(new User())

  def this() = {
    this(ActorSystem(classOf[UserTest].getSimpleName, ConfigFactory.parseString("ircserver.password=test")))
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An User actor" should {

    "reject invalid passwords" in {
      user ! User.Authenticate(password = "moep")
      expectMsg(User.InvalidPassword)
      user.stateName == User.AuhenticationPending
    }

    "accept valid passwords" in {
      user ! User.Authenticate(password = "test")
      user.stateName == User.RegistrationPending
    }

  }
}
