package ru.ifkbhit.ppo.database.table

import ru.ifkbhit.ppo.database._
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.model.UserStocks
import slick.lifted.Tag
import slick.sql.SqlProfile.ColumnOption.NotNull

class UserStocksTable(tag: Tag) extends Table[UserStocks](tag, "user_stocks") {

  def userId: Rep[Long] = column[Long]("user_id", NotNull)

  def stockId: Rep[Long] = column[Long]("stock_id", NotNull)

  def count: Rep[Long] = column[Long]("count", NotNull)

  override def * = (userId, stockId, count).mapTo[UserStocks]

  def pk = primaryKey("user_stocks_pk", (userId, stockId))
}

object UserStocksTable {

  val table: QT[UserStocksTable] = TableQuery[UserStocksTable]
}
