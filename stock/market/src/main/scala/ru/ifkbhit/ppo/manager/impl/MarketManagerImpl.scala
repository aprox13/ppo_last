package ru.ifkbhit.ppo.manager.impl

import ru.ifkbhit.ppo.database.actions.MarketActions
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.manager.MarketManager
import ru.ifkbhit.ppo.request.MarketRequest

import scala.concurrent.{ExecutionContext, Future}

class MarketManagerImpl(
  database: Database,
  marketActions: MarketActions,
)(
  implicit ec: ExecutionContext
) extends MarketManager {
  override def sell(marketRequest: MarketRequest): Future[Unit] = {
    import marketRequest._
    database.run(marketActions.sell(userId, stockId, count).transactionally)
  }

  override def buy(marketRequest: MarketRequest): Future[Unit] = {
    import marketRequest._
    database.run(marketActions.buy(userId, stockId, count).transactionally)
  }
}
