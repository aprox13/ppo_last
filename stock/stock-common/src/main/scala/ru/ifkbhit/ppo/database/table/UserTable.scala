package ru.ifkbhit.ppo.database.table

import ru.ifkbhit.ppo.database._
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.model.{Money, User}
import slick.lifted.Tag
import slick.sql.SqlProfile.ColumnOption.{NotNull, SqlType}

class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc, SqlType("SERIAL"))

  def balance: Rep[Money] = column[Money]("balance", NotNull)

  def name: Rep[String] = column[String]("name", NotNull)

  override def * = (id.?, balance, name).mapTo[User]
}

object UserTable {
  val table: QT[UserTable] = TableQuery[UserTable]
}