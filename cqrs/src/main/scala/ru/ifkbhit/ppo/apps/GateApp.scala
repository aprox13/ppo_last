package ru.ifkbhit.ppo.apps

import akka.actor.ActorSystem
import akka.stream.Materializer
import ru.ifkbhit.ppo.actions.{DefaultEventActions, DefaultManagerActions}
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.provider.ActorSystemProvider
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.handler.GateHandler
import ru.ifkbhit.ppo.manager.impl.GateManagerImpl
import ru.ifkbhit.ppo.util.{EventStoreConnectionProvider, SimpleTimeProvider, TimeProvider}

import scala.concurrent.ExecutionContext

object GateApp extends BaseApp {
  override def appRun(args: String*): Unit = {
    val cfg = AppConfig(currentConfig)

    implicit val actorSystem: ActorSystem = ActorSystemProvider.provide(cfg.gate.actorSystemConfig)
    implicit val mat: Materializer = Materializer(actorSystem)
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    implicit val timeProvider: TimeProvider = SimpleTimeProvider


    val db = EventStoreConnectionProvider.get(cfg.eventStoreConfig.connectionString)

    val events = new DefaultEventActions
    val managerActions = new DefaultManagerActions(events)
    val manager = new GateManagerImpl(db, events, managerActions)
    val handler = new GateHandler(manager)

    val apiService = new ApiService(cfg.gate.api, handler)
    apiService.bind()

    sys.addShutdownHook {
      db.close()
    }
  }
}
