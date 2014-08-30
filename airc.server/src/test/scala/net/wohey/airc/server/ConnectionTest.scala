package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.testkit._
import com.typesafe.config.ConfigFactory
import net.wohey.airc.{IrcMessage, UserIrcMessage}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.mock.MockitoSugar
import org.scalatest._

class ConnectionTest(_system: ActorSystem) extends TestKit(_system)
with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar with Inside {

  def this() = {
    this(ActorSystem(classOf[ConnectionTest].getSimpleName, ConfigFactory.parseString("ircserver.password=test")))
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  private class TestConnection extends ConnectionActor {

    val userProbe = TestProbe()

    override val remoteAddress = new InetSocketAddress(6667)

    override val user = userProbe.ref

    override val messageHandler = mock[IncomingMessageHandler]

  }

  "A connection" which {
    "is in the Open state" should {
      "stop if the incoming flow is closed" in {
        val probe = TestProbe()
        val connection = TestFSMRef(new TestConnection())
        probe watch connection
        connection ! Connection.IncomingFlowClosed
        probe expectTerminated connection
      }

      "accept a subscribe request" in {
        val connection = TestFSMRef(new TestConnection())
        val subscriber = mock[Subscriber[IrcMessage]]
        connection ! Connection.Subscribe(subscriber)
        connection.stateName should be(Connection.Subscribed)

        verify(subscriber, timeout(1000)).onSubscribe(any(classOf[Subscription]))
      }

      "process messages as soon as it is subscribe" in {
        val subscriber = mock[Subscriber[IrcMessage]]

        val connection = TestFSMRef(new TestConnection())
        connection ! new UserIrcMessage(None, "TEST", List.empty)
        connection ! new UserIrcMessage(None, "TEST", List.empty)
        connection.stateName should be(Connection.Open)
        inside(connection.stateData) { case Connection.Uninitialized(queue) =>
           queue should have size 2
        }

        connection ! Connection.Subscribe(subscriber)
        connection.stateName should be(Connection.Subscribed)

        verify(connection.underlyingActor.messageHandler, timeout(1000).times(2)).handleIncomingIrcMessage(any(classOf[UserIrcMessage]))
        verifyNoMoreInteractions(connection.underlyingActor.messageHandler)
      }
    }

  }
}
