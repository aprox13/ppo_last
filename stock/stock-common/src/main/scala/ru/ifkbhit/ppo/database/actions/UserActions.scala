package ru.ifkbhit.ppo.database.actions

import ru.ifkbhit.ppo.database.{DBRead, DBReadWrite, DBWrite}
import ru.ifkbhit.ppo.model.{Money, User}

trait UserActions {

  def insertOne(name: String): DBWrite[User]

  def addMoney(userId: Long, money: Money): DBReadWrite[User]

  def get(id: Long, forUpdate: Boolean): DBRead[User]
}
