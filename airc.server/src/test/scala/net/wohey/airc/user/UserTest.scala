package net.wohey.airc.user

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import net.wohey.airc.user.User.{Quit, InvalidPassword}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

class UserTest(_system: ActorSystem) extends TestKit(_system)
with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = {
    this(ActorSystem(classOf[UserTest].getSimpleName, ConfigFactory.parseString("ircserver.password=test")))
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An User actor" should {

    "reject invalid passwords" in {
      val user = TestFSMRef(new User())
      user ! User.Authenticate(password = "moep")
      expectMsg(User.InvalidPassword)
      user.stateName == User.AuhenticationPending
    }

    "accept valid passwords" in {
      val user = TestFSMRef(new User())
      user ! User.Authenticate(password = "test")
      user.stateName == User.RegistrationPending
    }

    "should be able to quit in any state" in {

      val userInAuhenticationPending = TestFSMRef(new User())
      userInAuhenticationPending ! User.Quit(Some("'cos i want to"))
      expectMsg(Quit)

      val userAuthenticated = TestFSMRef(new User())
      userAuthenticated ! User.Authenticate(password = "test")
      userAuthenticated ! User.Quit(Some("'cos i want to"))
      expectMsg(Quit)
    }

  }
}
