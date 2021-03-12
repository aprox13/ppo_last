package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.manager._

import scala.concurrent.Future

trait ManagersManager {

  def getUser(getUserCommand: GetUserCommand): Future[UserResult]

  def addUser(payload: UserPayload): Future[UserResult]

  def renewPass(renewPassCommand: RenewPassCommand): Future[RenewPassPayload]
}

object ManagersManager {

  case object PositiveDaysRequired extends RuntimeException("Required positive day count to renew")

}