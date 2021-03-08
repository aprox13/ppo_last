package ru.ifkbhit.ppo.akka.manager.impl

import akka.actor.{ActorSystem, Props}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import ru.ifkbhit.ppo.akka.actor.ApiActor
import ru.ifkbhit.ppo.akka.config.ApiActorConfig
import ru.ifkbhit.ppo.akka.manager.{ApiManager, SearchManager}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class ApiManagerImpl(searchManager: SearchManager, apiActorConfig: ApiActorConfig)(implicit system: ActorSystem)
  extends ApiManager
    with Logging {
  override def searchRequest(
    request: String
  )(
    implicit ec: ExecutionContext
  ): Future[Response] = {

    val actor = system.actorOf(
      Props(
        classOf[ApiActor],
        searchManager,
        apiActorConfig.collectingTimeout
      ),
      "master-api"
    )

    implicit val timeout: Timeout = Timeout(apiActorConfig.fullTimeout)

    ask(actor, ApiActor.ApiRequestMessage(request))
      .mapTo[Response]
      .recover {
        case e: AskTimeoutException =>
          log.error("Timeout in manager", e)
          Response.failed("Server timeout")
        case e =>
          log.error("Unexpected error while asking", e)
          Response.failed("Internal error")
      }
  }
}

