package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.manager.{RenewPassPayload, UserPayload, UserResult}

import scala.concurrent.Future

trait ManagersManager {

  def getUser(userId: Long): Future[UserResult]

  def addUser(payload: UserPayload): Future[UserResult]

  def renewPass(userId: Long, days: Int): Future[RenewPassPayload]
}

object ManagersManager {

  case object PositiveDaysRequired extends RuntimeException("Required positive day count to renew")

}