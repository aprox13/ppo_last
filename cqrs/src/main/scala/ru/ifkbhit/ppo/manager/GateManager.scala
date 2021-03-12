package ru.ifkbhit.ppo.manager


import scala.concurrent.Future

trait GateManager {
  def enter(userId: Long): Future[String]

  def exit(userId: Long): Future[String]
}

object GateManager {

  case object UserHasNoPass extends RuntimeException("User has no pass")

  case object UserAlreadyEnter extends RuntimeException("User already enter")

  case object UserAlreadyExit extends RuntimeException("User already exit")

  case object UserNotEnterYet extends RuntimeException("User not enter yet")

}