package ru.ifkbhit.ppo.common.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.handler.Handler
import ru.ifkbhit.ppo.common.model.config.ApiConfig

import scala.concurrent.Await
import scala.util.Success


class ApiService(
  apiConfig: ApiConfig,
  handler: Handler
)(
  implicit system: ActorSystem,
  materializer: Materializer
) extends Logging {

  def bind(): Unit = {
    val url = apiConfig.endpoint.toUrl
    import system.dispatcher

    val bindingF =
      Http().newServerAt(apiConfig.endpoint.host, apiConfig.endpoint.port).bindFlow(handler.route)

    bindingF.onComplete {
      case Success(bindingResult) =>

        log.info(s"Started listening $url")

        Runtime.getRuntime.addShutdownHook(new Thread() {
          override def run(): Unit = {
            log.info(s"Shutting down server on $url")
            Await.result(
              bindingResult.unbind()
                .flatMap { _ =>
                  log.info(s"Ended listening to $url")
                  system.terminate()
                }.recover {
                case exception =>
                  log.error(s"Couldn't end listening for $url", exception)
                  System.exit(1)
              }, apiConfig.unbindTimeout
            )
          }
        }
        )

    }

  }
}