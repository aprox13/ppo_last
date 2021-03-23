package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.manager.StockManager
import ru.ifkbhit.ppo.manager.impl.StockManagerImpl

trait StockManagerComponents {

  def stockManager: StockManager
}

trait DefaultStockManagerComponents extends StockManagerComponents {
  self: DatabaseComponents with StockActionsComponents =>

  override def stockManager: StockManager = new StockManagerImpl(database, stockActions)
}
