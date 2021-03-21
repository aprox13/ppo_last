package ru.ifkbhit.ppo.database.actions

import ru.ifkbhit.ppo.database.DBRead
import ru.ifkbhit.ppo.model.response.user.UserStockInfo

trait UserMarketActions {
  def getStocks(userId: Long): DBRead[Seq[UserStockInfo]]
}
