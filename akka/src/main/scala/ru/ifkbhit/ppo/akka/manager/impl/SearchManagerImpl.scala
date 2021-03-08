package ru.ifkbhit.ppo.akka.manager.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ru.ifkbhit.ppo.akka.config.EnginesConfig
import ru.ifkbhit.ppo.akka.http.HttpClient
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.ExecutionContext

class SearchManagerImpl(
  enginesConfig: EnginesConfig, httpClient: HttpClient
)(implicit ec: ExecutionContext) extends SearchManager with Logging with SprayJsonSupport {

  import SearchResponse._

  override def searchFrom(engineName: String, request: String): Response = {
    enginesConfig.engines.get(engineName)
      .map { cfg =>
        val url = cfg.endpoint.toUrl
        val path = s"$url?request=$request"
        httpClient.doGet[SearchResponse](path)
      }.getOrElse {
      Response.FailedResponse[SearchResponse](s"Unsupported engine $engineName")
    }

    ???
  }

  override def engines: Seq[String] = enginesConfig.engines.keys.toSeq

  private def internalError(logMessage: String, throwable: Throwable): Response[SearchResponse] = {
    log.error(logMessage, throwable)

    Response.FailedResponse("Internal error!")
  }
}
