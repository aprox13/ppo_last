package ru.ifkbhit.ppo.database.table

import ru.ifkbhit.ppo.database._
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.model.{Money, StockItem}
import slick.lifted.{ProvenShape, Tag}
import slick.sql.SqlProfile.ColumnOption.{NotNull, SqlType}

class StockItemTable(tag: Tag) extends Table[StockItem](tag, "stocks") {

  val id: Rep[Long] = column[Long]("stock_id", O.AutoInc, O.PrimaryKey, SqlType("SERIAL"))
  val name: Rep[String] = column[String]("name", NotNull)
  val count: Rep[Long] = column[Long]("count", NotNull)
  val price: Rep[Money] = column[Money]("price", NotNull)

  override def * : ProvenShape[StockItem] = (id.?, name, price, count) <> ((StockItem.apply _).tupled, StockItem.unapply)
}

object StockItemTable {
  val stockTable: QT[StockItemTable] = TableQuery[StockItemTable]
}
