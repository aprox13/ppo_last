package ru.ifkbhit.ppo.apps

import akka.actor.ActorSystem
import akka.stream.Materializer
import ru.ifkbhit.ppo.actions.{DefaultEventActions, DefaultManagerActions}
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.provider.ActorSystemProvider
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.handler.ManagerHandler
import ru.ifkbhit.ppo.manager.impl.ManagerManagerImpl
import ru.ifkbhit.ppo.util.EventStoreProvider

import scala.concurrent.ExecutionContext

object ManagersApp extends BaseApp {


  override def appRun(args: String*): Unit = {
    val cfg = AppConfig(currentConfig)

    implicit val actorSystem: ActorSystem = ActorSystemProvider.provide(cfg.managers.actorSystemConfig)
    implicit val mat: Materializer = Materializer(actorSystem)
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    val db = EventStoreProvider.get(cfg.eventStoreConfig.connectionString)

    val events = DefaultEventActions
    val managerActions = new DefaultManagerActions(events)
    val manager = new ManagerManagerImpl(db, events, managerActions)
    val handler = new ManagerHandler(manager)

    val apiService = new ApiService(cfg.managers.api, handler)
    apiService.bind()

    sys.addShutdownHook {
      db.close()
    }
  }
}
