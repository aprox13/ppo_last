package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.model.manager.UserPayload

import scala.concurrent.Future

trait ManagersManager {

  def getUser(userId: Long): Future[Response]

  def addUser(payload: UserPayload): Future[Response]

  def renewPass(userId: Long, days: Int): Future[Response]
}
