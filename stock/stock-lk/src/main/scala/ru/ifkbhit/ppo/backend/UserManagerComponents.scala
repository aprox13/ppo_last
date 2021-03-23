package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.manager.UserManager
import ru.ifkbhit.ppo.manager.impl.UserManagerImpl

trait UserManagerComponents {

  def userManager: UserManager
}

trait DefaultUserManagerComponents extends UserManagerComponents {
  self: DatabaseComponents
    with ExecutionContextComponents
    with UserActionsComponents
    with UserMarketActionsComponents =>

  override def userManager: UserManager = new UserManagerImpl(database, userActions, userMarketActions)
}
