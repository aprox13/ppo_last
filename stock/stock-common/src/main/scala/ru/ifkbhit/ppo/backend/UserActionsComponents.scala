package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.database.actions.UserActions
import ru.ifkbhit.ppo.database.actions.impl.UserActionsImpl

trait UserActionsComponents {

  def userActions: UserActions
}

trait DefaultUserActionsComponents extends UserActionsComponents {
  self: ExecutionContextComponents =>

  override def userActions: UserActions = new UserActionsImpl
}
