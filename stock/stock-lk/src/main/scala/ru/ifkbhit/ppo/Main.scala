package ru.ifkbhit.ppo

import ru.ifkbhit.ppo.backend._
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.model.config.ActorSystemConfig
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.database.provider.DbConfig
import ru.ifkbhit.ppo.handler.UserHandler

object Main extends BaseApp {

  class LkBackend(
    override val actorSystemConfig: ActorSystemConfig,
    override val dbConfig: DbConfig
  ) extends DefaultAkkaComponents
    with DefaultDatabaseComponents
    with DefaultExecutionContextComponents
    with DefaultUserActionsComponents
    with DefaultUserMarketActionsComponents
    with DefaultUserManagerComponents

  override def appRun(args: String*): Unit = {
    val cfg = AppConfig(currentConfig)

    val backend = new LkBackend(cfg.actorSystemConfig, cfg.dbConfig)

    val handler = new UserHandler(backend.userManager)(backend.ec)

    val apiService = new ApiService(cfg.api, handler)(backend.actorSystem, backend.materializer)
    apiService.bind()
  }
}
