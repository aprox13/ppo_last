package ru.ifkbhit.ppo.database.actions

import ru.ifkbhit.ppo.database.{DBRead, DBReadWrite, DBWrite}
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request.{StockItemCreateRequest, StockItemPatch}

trait StockActions {

  def insertBatch(requests: Seq[StockItemCreateRequest]): DBWrite[Seq[StockItem]]

  def get(id: Long, forUpdate: Boolean): DBRead[StockItem]

  def patch(id: Long, patches: Seq[StockItemPatch]): DBReadWrite[StockItem]

}
