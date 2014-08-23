package net.wohey.airc.user

import akka.actor.{FSM, Actor}
import net.wohey.airc.user.User._

class User extends Actor with FSM[UserState, UserData] {


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
      sender ! AlreadyAuthenticated
      stay()
  }

}


object User {

  case class Authenticate(password: String)

  case object InvalidPassword

  case object AlreadyAuthenticated



  sealed trait UserState

  case object AuhenticationPending extends UserState

  case object RegistrationPending extends UserState




  sealed trait UserData

  case object EmptyState extends UserData

}
