package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request.{StockItemCreateRequest, StockItemPatchRequest}

import scala.concurrent.Future

trait StockManager {

  def getStock(id: Long): Future[StockItem]

  def addStocks(createRequests: Seq[StockItemCreateRequest]): Future[Seq[StockItem]]

  def patchStocks(stockItemPatchRequests: Seq[StockItemPatchRequest]): Future[Seq[StockItem]]
}
