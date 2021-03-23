package ru.ifkbhit.ppo.database.actions.impl

import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.database.actions.StockActions
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.table.StockItemTable
import ru.ifkbhit.ppo.database.{DBRead, DBReadWrite, DBWrite}
import ru.ifkbhit.ppo.exception.ItemNotFound
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request.{StockItemCreateRequest, StockItemPatch}

import scala.concurrent.ExecutionContext

class StockActionsImpl(implicit ec: ExecutionContext) extends StockActions {

  import StockItemTable._

  override def insertBatch(requests: Seq[StockItemCreateRequest]): DBWrite[Seq[StockItem]] = {
    val newStocks = requests.map(StockItem.createForInsert)

    for {
      ids <- (stockTable returning stockTable.map(_.id)) ++= newStocks
    } yield newStocks.zip(ids).map {
      case (stock, id) => stock.copy(id = Some(id))
    }
  }

  override def get(id: Long, forUpdate: Boolean): DBRead[StockItem] =
    for {
      userOpt <- stockTable.filter(_.id === id).take(1)
        .applyTransformIf(forUpdate)(_.forUpdate)
        .result.headOption
    } yield userOpt.getOrElse(throw ItemNotFound(id))


  override def patch(id: Long, patches: Seq[StockItemPatch]): DBReadWrite[StockItem] = {
    for {
      item <- get(id, forUpdate = true)
      patched = applyPatches(patches, item)
      _ <- stockTable.filter(_.id === id).update(patched)
    } yield patched
  }


  private def applyPatches(patches: Seq[StockItemPatch], stockItem: StockItem): StockItem = {
    patches.foldLeft(stockItem) {
      case (i, StockItemPatch.FullPatch(priceNew, countNew)) =>
        i.copy(
          price = priceNew,
          count = countNew
        )

      case (i, StockItemPatch.PricePatch(price)) =>
        i.copy(price = price)
      case (i, StockItemPatch.CountPatch(count)) =>
        i.copy(count = count)
    }

  }
}
