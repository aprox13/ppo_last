package ru.ifkbhit.ppo.manager.impl

import ru.ifkbhit.ppo.database.actions.StockActions
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.manager.StockManager
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request.{StockItemCreateRequest, StockItemPatchRequest}

import scala.concurrent.Future

class StockManagerImpl(database: Database, stockActions: StockActions)
  extends StockManager {

  override def getStock(id: Long): Future[StockItem] =
    database.run(
      stockActions.get(id, forUpdate = false).transactionally
    )

  override def addStocks(createRequests: Seq[StockItemCreateRequest]): Future[Seq[StockItem]] =
    database.run(
      stockActions.insertBatch(createRequests).transactionally
    )

  override def patchStocks(stockItemPatchRequests: Seq[StockItemPatchRequest]): Future[Seq[StockItem]] =
    database.run(
      DBIO.sequence(stockItemPatchRequests.map(r => stockActions.patch(r.id, r.patches))).transactionally
    )
}
