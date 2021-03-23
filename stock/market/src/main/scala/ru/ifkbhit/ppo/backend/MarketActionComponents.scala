package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.database.actions.MarketActions
import ru.ifkbhit.ppo.database.actions.impl.MarketActionsImpl

trait MarketActionComponents {
  def marketActions: MarketActions
}

trait DefaultMarketActionComponents extends MarketActionComponents {
  self: UserActionsComponents with StockActionsComponents with ExecutionContextComponents =>

  override def marketActions: MarketActions = new MarketActionsImpl(userActions, stockActions)
}