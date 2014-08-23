package net.wohey.airc.server

import java.net.InetSocketAddress

import akka.actor.{FSM, ActorLogging, Actor}
import net.wohey.airc.{NoticeMessage, IrcMessage, IncomingIrcMessage}
import net.wohey.airc.server.Connection._
import org.reactivestreams.{Subscriber, Subscription}
import scala.collection.immutable.Queue
import scala.concurrent.duration._

class Connection(remoteAddress: InetSocketAddress) extends Actor with ActorLogging with FSM[Connection.ConnectionState, Connection.ConnectionData] {

  private class ConnectionSubscription extends Subscription {
    override def cancel() = self ! OutgoingFlowClosed

    override def request(n: Int) = self ! SubscriptionRequest(n)
  }

  startWith(Open, Uninitialized(messages = Queue.empty))

  when(Open, stateTimeout = 1.second) {
    case Event(IncomingFlowClosed, _) =>
      log.info(s"$remoteAddress closed connection")
      stop()
    case Event(message : IncomingIrcMessage, Uninitialized(messages)) =>
      log.debug("{}: buffered message before subscription", remoteAddress)
      stay() using Uninitialized(messages = messages enqueue message)
    case Event(Subscribe(subscriber), Uninitialized(messages)) =>
      subscriber.onSubscribe(new ConnectionSubscription)
      messages.foreach(self ! _)
      goto(Subscribed) using SubscriptionData(requested = 0, subscriber)
    case Event(StateTimeout, _) =>
      log.warning("{} connection wasn't subscribed", remoteAddress)
      stop()
  }

  when(Subscribed) {
    case Event(message : IncomingIrcMessage, _) =>
      log.debug("{}: {}", remoteAddress, message)
      stay()
    case Event(SubscriptionRequest(n), SubscriptionData(requested, subscriber)) =>
      log.debug(s"Subscription request $n")
      subscriber.onNext(NoticeMessage(serverName = "test", nick = "todo"))
      stay()
  }




}

object Connection {
  case class IncomingFlowClosed()
  case class OutgoingFlowClosed()
  case class Subscribe(subscriber : Subscriber[IrcMessage])
  case class SubscriptionRequest(n : Int)

  sealed trait ConnectionState
  case object Open extends ConnectionState
  case object Subscribed extends ConnectionState

  sealed trait ConnectionData
  case class Uninitialized(messages : Queue[IncomingIrcMessage]) extends ConnectionData
  case class SubscriptionData(requested : Int, subscriber : Subscriber[IrcMessage]) extends ConnectionData

}




