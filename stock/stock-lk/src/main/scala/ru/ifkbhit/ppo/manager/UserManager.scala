package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.Money
import ru.ifkbhit.ppo.model.response.user.UserInfo
import ru.ifkbhit.ppo.request.UserCreateRequest

import scala.concurrent.Future

trait UserManager {

  def getUserInfo(userId: Long): Future[UserInfo]

  def addUser(request: UserCreateRequest): Future[UserInfo]

  def addMoney(userId: Long, money: Money): Future[UserInfo]
}
