package ru.ifkbhit.ppo.akka


import akka.actor.ActorSystem
import akka.stream.Materializer
import ru.ifkbhit.ppo.akka.config.AppConfig
import ru.ifkbhit.ppo.akka.handler.ApiHandler
import ru.ifkbhit.ppo.akka.manager.impl.{ApiManagerImpl, SearchManagerImpl}
import ru.ifkbhit.ppo.common.provider.ActorSystemProvider
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.common.{BaseApp, Logging}

import scala.concurrent.ExecutionContext


object Main extends BaseApp with Logging {

  override def appRun(args: String*): Unit = {
    val appConfig = AppConfig.provide
    implicit val actorSystem: ActorSystem = ActorSystemProvider.provide(appConfig.actorSystemConfig)
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: Materializer = Materializer(actorSystem)

    val searchManager = new SearchManagerImpl(appConfig.enginesConfig)
    val apiManager = new ApiManagerImpl(searchManager, appConfig.apiActorConfig)
    val apiHandler = new ApiHandler(apiManager)


    new ApiService(appConfig.apiConfig, apiHandler).bind()

  }

}
