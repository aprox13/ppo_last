package ru.ifkbhit.ppo.components

import ru.ifkbhit.ppo.TestExecutionContextComponents
import ru.ifkbhit.ppo.backend.DatabaseComponents
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.UserStocksTable

import scala.concurrent.Future

trait TestDbComponents {

  self: DatabaseComponents with TestExecutionContextComponents =>

  def getAllUserStocks: Future[Seq[(Long, Long, Long)]] =
    database.run(
      UserStocksTable.table.result.map { xx =>
        xx.map { x =>
          (x.userId, x.stockId, x.count)
        }
      }
    )

}
