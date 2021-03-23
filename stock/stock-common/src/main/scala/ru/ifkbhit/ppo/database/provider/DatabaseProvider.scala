package ru.ifkbhit.ppo.database.provider

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.{StockItemTable, UserStocksTable, UserTable}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object DatabaseProvider extends Logging {

  def provide(dbConfig: DbConfig): Database = {
    val hikariConfig = new HikariConfig
    hikariConfig.setJdbcUrl(dbConfig.connectionString)
    hikariConfig.setUsername(dbConfig.user)
    dbConfig.password.foreach(hikariConfig.setPassword)

    val ds = new HikariDataSource(hikariConfig)
    val db = Database.forDataSource(ds, maxConnections = dbConfig.maxConnections)

    if (dbConfig.createSchema) {
      Await.result(
        db.run(
          DBIO.seq(
            UserStocksTable.table.schema.createIfNotExists,
            UserTable.table.schema.createIfNotExists,
            StockItemTable.stockTable.schema.createIfNotExists
          )
        ),
        20.seconds
      )
    }

    val tables = Await.result(
      db.run(sql"SELECT tablename, tableowner FROM pg_catalog.pg_tables;".as[(String, String)]),
      20.seconds
    )

    tables.foreach {
      case (name, owner) =>
        log.info(s"Found table $name owned by $owner")
    }

    db
  }

}
