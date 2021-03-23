package ru.ifkbhit.ppo.manager.impl

import ru.ifkbhit.ppo.database.actions.{UserActions, UserMarketActions}
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.manager.UserManager
import ru.ifkbhit.ppo.model.Money
import ru.ifkbhit.ppo.model.response.user.UserInfo
import ru.ifkbhit.ppo.request.UserCreateRequest

import scala.concurrent.{ExecutionContext, Future}

class UserManagerImpl(
  database: Database,
  userActions: UserActions,
  market: UserMarketActions
)(
  implicit ec: ExecutionContext
) extends UserManager {
  override def getUserInfo(userId: Long): Future[UserInfo] = {
    val action = for {
      user <- userActions.get(userId, forUpdate = false)
      stocks <- market.getStocks(userId)
    } yield UserInfo.build(user, stocks)

    database.run(action.transactionally)
  }


  override def addUser(request: UserCreateRequest): Future[UserInfo] =
    database.run(
      userActions.insertOne(request.name)
    ).map(UserInfo.build(_, Seq.empty))

  override def addMoney(userId: Long, money: Money): Future[UserInfo] =
    database.run(userActions.addMoney(userId, money).transactionally)
      .flatMap(_ => getUserInfo(userId))
}
