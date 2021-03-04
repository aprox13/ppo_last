package ru.ifkbhit.ppo.akka.manager.impl

import java.io.InputStream

import ru.ifkbhit.ppo.akka.config.EnginesConfig
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.common.{Logging}
import ru.ifkbhit.ppo.common.model.response.Response

import scala.util.{Failure, Success, Try}

class SearchManagerImpl(enginesConfig: EnginesConfig) extends SearchManager with Logging {
  override def searchFrom(engineName: String, request: String): Response[SearchResponse] = {
    enginesConfig.engines.get(engineName)
      .map { cfg =>
        val url = cfg.endpoint.toUrl
        val path = s"$url?request=$request"
//        httpClient.get(path)(parseResponse) match {
//          case Failure(exception) =>
//            internalError(s"Error while processing request $request to $engineName", exception)
//          case Success(value) =>
//            log.info(s"Success for ${engineName}")
//            value
//        }
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
