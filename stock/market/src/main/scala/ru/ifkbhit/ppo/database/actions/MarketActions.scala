package ru.ifkbhit.ppo.database.actions

import ru.ifkbhit.ppo.database.DBReadWrite

trait MarketActions {
  def buy(userId: Long, stockId: Long, count: Long): DBReadWrite[Unit]

  def sell(userId: Long, stockId: Long, count: Long): DBReadWrite[Unit]
}
