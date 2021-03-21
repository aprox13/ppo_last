package ru.ifkbhit.ppo

import ru.ifkbhit.ppo.backend._
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.handler.PingRoute
import ru.ifkbhit.ppo.common.model.config.ActorSystemConfig
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.database.provider.DbConfig
import ru.ifkbhit.ppo.handler.MarketHandler

object Main extends BaseApp {

  class MarketBackend(
    override val actorSystemConfig: ActorSystemConfig,
    override val dbConfig: DbConfig
  ) extends DefaultAkkaComponents
    with DefaultDatabaseComponents
    with DefaultExecutionContextComponents
    with DefaultMarketActionComponents
    with DefaultMarketManagerComponents
    with DefaultStockActionsComponents
    with DefaultUserActionsComponents
    with DefaultStockManagerComponents


  override def appRun(args: String*): Unit = {
    val cfg = AppConfig(currentConfig)

    val backend = new MarketBackend(cfg.actorSystemConfig, cfg.dbConfig)

    val handler = new MarketHandler(backend.marketManager, backend.stockManager)(backend.ec) with PingRoute

    val apiService = new ApiService(cfg.api, handler)(backend.actorSystem, backend.materializer)
    apiService.bind()
  }

}
