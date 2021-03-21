package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.database.actions.UserMarketActions
import ru.ifkbhit.ppo.database.actions.impl.UserMarketActionsImpl

trait UserMarketActionsComponents {

  def userMarketActions: UserMarketActions
}

trait DefaultUserMarketActionsComponents extends UserMarketActionsComponents {

  self: UserActionsComponents with ExecutionContextComponents =>

  override def userMarketActions: UserMarketActions = new UserMarketActionsImpl(userActions)
}
