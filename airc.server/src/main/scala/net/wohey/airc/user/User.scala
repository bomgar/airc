package net.wohey.airc.user

import akka.actor.{ActorLogging, FSM, Actor}
import net.wohey.airc.user.User._
import net.wohey.airc.AlreadyAuthenticatedMessage

class User extends Actor with ActorLogging with FSM[UserState, UserData] {


  private val serverPassword = context.system.settings.config.getString("ircserver.password")

  startWith(AuhenticationPending, EmptyState)

  when(AuhenticationPending) {
    case Event(Authenticate(password), _) =>
      if (password == serverPassword) {
        goto(RegistrationPending)
      }
      else {
        sender ! InvalidPassword
        stay()
      }
  }

  when(RegistrationPending) {
    case Event(Authenticate(_), _) =>
      sender ! new AlreadyAuthenticatedMessage("todo")
      stay()
    case Event(SetUsername(username), _) =>
      log.info(s"STUB: User wishes to change nickname to $username")
      stay()
  }

  whenUnhandled {
    case Event(Quit(message), _) =>
      val quitMessage = message match {
        case Some(msg) => s" with message '$msg'"
        case None => "with no specific message"
      }
      log.info(s"User wishes to close connection $quitMessage.")
      sender ! Quit
      stop()
  }

}


object User {

  case class Authenticate(password: String)

  case class SetUsername(username: String)

  case class Quit(message: Option[String])

  case object InvalidPassword


  sealed trait UserState

  case object AuhenticationPending extends UserState

  case object RegistrationPending extends UserState

  case object Auhenticated extends UserState



  sealed trait UserData

  case object EmptyState extends UserData

}
