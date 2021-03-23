package ru.ifkbhit.ppo.database.actions.impl

import ru.ifkbhit.ppo.database.actions.{UserActions, UserMarketActions}
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.{StockItemTable, UserStocksTable}
import ru.ifkbhit.ppo.database.{DBRead, _}
import ru.ifkbhit.ppo.model.response.user.UserStockInfo

import scala.concurrent.ExecutionContext


class UserMarketActionsImpl(userActions: UserActions)(implicit ec: ExecutionContext) extends UserMarketActions {

  override def getStocks(userId: Long): DBRead[Seq[UserStockInfo]] = {
    val joined =
      for {
        (stock, userStock) <- StockItemTable.stockTable join UserStocksTable.table on (_.id === _.stockId)
      } yield (userStock.userId, stock.name, stock.price, userStock.count)

    for {
      _ <- userActions.get(userId, forUpdate = false)
      result <- joined.filter(_._1 === userId).result
    } yield result.map {
      case (_, name, price, count) => UserStockInfo(name, count, price)
    }
  }
}
