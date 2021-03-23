package ru.ifkbhit.ppo.components

import ru.ifkbhit.ppo.backend.DatabaseComponents
import ru.ifkbhit.ppo.utils.BasicMarketContainer

trait MarketInContainerComponents {

  self: DatabaseComponents =>

  identity(database) // force init database

  lazy val marketContainer: BasicMarketContainer =
    new BasicMarketContainer(8080, dbConfig)

  sys.addShutdownHook(marketContainer.stop())
  marketContainer.start()
}
