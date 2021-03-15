package ru.ifkbhit.ppo.akka.manager.impl

import akka.actor.{ActorSystem, Props}
import ru.ifkbhit.ppo.akka.actor.ApiActor
import ru.ifkbhit.ppo.akka.config.ApiActorConfig
import ru.ifkbhit.ppo.akka.manager.ApiManager.ApiResponse
import ru.ifkbhit.ppo.akka.manager.{ApiManager, SearchManager}
import ru.ifkbhit.ppo.common.Logging

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps

class ApiManagerImpl(searchManager: SearchManager, apiActorConfig: ApiActorConfig)(implicit system: ActorSystem)
  extends ApiManager
    with Logging {
  override def searchRequest(
    request: String
  )(
    implicit ec: ExecutionContext
  ): Future[ApiResponse] = {
    val result = Promise[ApiResponse]()
    val actor = system.actorOf(
      Props(
        classOf[ApiActor],
        searchManager,
        apiActorConfig.collectingTimeout,
        result,
        ec
      ),
      s"master-api-${System.currentTimeMillis()}"
    )

    actor ! ApiActor.ApiRequestMessage(request)

    result.future
  }
}

