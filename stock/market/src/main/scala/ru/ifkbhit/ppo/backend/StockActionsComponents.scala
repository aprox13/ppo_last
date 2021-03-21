package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.database.actions.StockActions
import ru.ifkbhit.ppo.database.actions.impl.StockActionsImpl

trait StockActionsComponents {

  def stockActions: StockActions
}

trait DefaultStockActionsComponents extends StockActionsComponents {
  self: ExecutionContextComponents =>

  override def stockActions: StockActions = new StockActionsImpl
}
