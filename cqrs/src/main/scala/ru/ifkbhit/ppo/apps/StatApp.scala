package ru.ifkbhit.ppo.apps

import java.util.concurrent.{Executors, ScheduledExecutorService}

import akka.actor.ActorSystem
import akka.stream.Materializer
import ru.ifkbhit.ppo.actions.DefaultEventActions
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.provider.{ActorSystemProvider, CachedProvider, Provider}
import ru.ifkbhit.ppo.common.service.ApiService
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.handler.StatHandler
import ru.ifkbhit.ppo.manager.impl.StatManagerImpl
import ru.ifkbhit.ppo.manager.stat.{StatStorageProvider, UserStatStorage}
import ru.ifkbhit.ppo.util.{EventStoreConnectionProvider, SimpleTimeProvider, TimeProvider}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object StatApp extends BaseApp {


  override def appRun(args: String*): Unit = {
    val cfg = AppConfig(currentConfig)

    implicit val actorSystem: ActorSystem = ActorSystemProvider.provide(cfg.managers.actorSystemConfig)
    implicit val mat: Materializer = Materializer(actorSystem)
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    implicit val timeProvider: TimeProvider = SimpleTimeProvider

    val db = EventStoreConnectionProvider.get(cfg.eventStoreConfig.connectionString)

    val events = new DefaultEventActions
    val statScheduler = Executors.newScheduledThreadPool(1)

    val statProvider: Provider[UserStatStorage] =
      new StatStorageProvider(db, events)
        with CachedProvider[UserStatStorage] {
        override protected def scheduler: ScheduledExecutorService = statScheduler

        override protected def refreshDelay: FiniteDuration = 10.minutes
      }


    val manager = new StatManagerImpl(statProvider)
    val handler = new StatHandler(manager)

    val apiService = new ApiService(cfg.stat.api, handler)
    apiService.bind()

    sys.addShutdownHook {
      db.close()
    }
  }
}
