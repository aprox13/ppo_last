package ru.ifkbhit.ppo.database.actions.impl

import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.database.actions.UserActions
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.UserTable
import ru.ifkbhit.ppo.database.{DBRead, DBReadWrite, DBWrite}
import ru.ifkbhit.ppo.exception.UserNotFound
import ru.ifkbhit.ppo.model.{Money, User}

import scala.concurrent.ExecutionContext


class UserActionsImpl(implicit ec: ExecutionContext) extends UserActions {

  import UserTable._

  override def insertOne(name: String): DBWrite[User] = {
    val newUser = User(None, Money.NoMoney, name)

    for {
      id <- (table returning table.map(_.id)) += newUser
    } yield newUser.copy(id = Some(id))
  }

  override def addMoney(userId: Long, money: Money): DBReadWrite[User] =
    for {
      user <- get(userId, forUpdate = true)
      updated = user.copy(balance = user.balance + money)
      _ <- table.filter(_.id === userId).update(updated)
    } yield updated

  override def get(id: Long, forUpdate: Boolean): DBRead[User] =
    table.filter(_.id === id).take(1)
      .applyTransformIf(forUpdate)(_.forUpdate)
      .result
      .headOption.map(_.getOrElse(throw UserNotFound(id)))
}
