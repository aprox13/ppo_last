package ru.ifkbhit.ppo.database.actions.impl

import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.database.actions.{MarketActions, StockActions, UserActions}
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.{UserTable, _}
import ru.ifkbhit.ppo.database.{DBRead, DBReadWrite, requireAction}
import ru.ifkbhit.ppo.exception.{NotEnoughMoney, NotEnoughStocks, NotEnoughStocksAtUser}
import ru.ifkbhit.ppo.model.Money._
import ru.ifkbhit.ppo.model.UserStocks

import scala.concurrent.ExecutionContext
import scala.math.Ordering.Implicits._

class MarketActionsImpl(userActions: UserActions, stockActions: StockActions)(implicit ec: ExecutionContext) extends MarketActions {

  import UserStocksTable._

  override def buy(userId: Long, stockId: Long, count: Long): DBReadWrite[Unit] =
    for {
      user <- userActions.get(userId, forUpdate = true)
      stock <- stockActions.get(stockId, forUpdate = true)
      nowStock <- getStockOpt(userId, stockId)
        .map(_.getOrElse(UserStocks(userId, stockId, 0)))

      amount = stock.price * count

      _ <- requireAction(stock.count >= count, NotEnoughStocks)
      _ <- requireAction(user.balance >= amount, NotEnoughMoney)

      updated = nowStock.copy(count = nowStock.count + count)

      _ <- UserTable.table.filter(_.id === userId).update(user.copy(balance = user.balance - amount))
      _ <- StockItemTable.stockTable.filter(_.id === stockId).update(stock.copy(count = stock.count - count))
      _ <- table insertOrUpdate updated
    } yield ()

  override def sell(userId: Long, stockId: Long, count: Long): DBReadWrite[Unit] =
    for {
      user <- userActions.get(userId, forUpdate = true)
      stock <- stockActions.get(stockId, forUpdate = true)
      nowStock <- getStockOpt(userId, stockId)
        .map(_.filter(_.count >= count))
        .map(_.getOrElse(throw NotEnoughStocksAtUser))

      amount = stock.price * count
      updated = nowStock.copy(count = nowStock.count - count)

      _ <- UserTable.table.filter(_.id === userId).update(user.copy(balance = user.balance + amount))
      _ <- StockItemTable.stockTable.filter(_.id === stockId).update(stock.copy(count = stock.count + count))

      _ <- table.filter(_.stockId === stockId).filter(_.userId === userId)
        .applyTransform {
          case target if updated.count == 0 =>
            target.delete
          case target =>
            target.update(updated)
        }

    } yield ()

  private def getStockOpt(userId: Long, stockId: Long): DBRead[Option[UserStocks]] =
    for {
      nowStock <- table.filter(r => r.stockId === stockId && r.userId === userId)
        .forUpdate
        .take(1)
        .result
        .headOption
    } yield nowStock
}
