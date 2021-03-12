package ru.ifkbhit.ppo.manager


import ru.ifkbhit.ppo.model.gate.{UserEnterCommand, UserExitCommand}

import scala.concurrent.Future

trait GateManager {
  def enter(userEnterCommand: UserEnterCommand): Future[String]

  def exit(userExitCommand: UserExitCommand): Future[String]
}

object GateManager {

  case object UserHasNoPass extends RuntimeException("User has no pass")

  case object UserAlreadyEnter extends RuntimeException("User already enter")

  case object UserAlreadyExit extends RuntimeException("User already exit")

  case object UserNotEnterYet extends RuntimeException("User not enter yet")

}