package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.manager.MarketManager
import ru.ifkbhit.ppo.manager.impl.MarketManagerImpl

trait MarketManagerComponents {

  def marketManager: MarketManager
}

trait DefaultMarketManagerComponents extends MarketManagerComponents {
  self: DatabaseComponents with MarketActionComponents with ExecutionContextComponents =>

  override def marketManager: MarketManager = new MarketManagerImpl(database, marketActions)
}
