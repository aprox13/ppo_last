package ru.ifkbhit.ppo.akka.manager.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ru.ifkbhit.ppo.akka.config.EnginesConfig
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.manager.SearchManager.UnsupportedEngine
import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response
import scalaj.http._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class SearchManagerImpl(
  enginesConfig: EnginesConfig
) extends SearchManager with Logging with SprayJsonSupport {

  import SearchResponse._

  override def searchFrom(engineName: String, request: String)(implicit ec: ExecutionContext): Future[Response] =
    Future {
      enginesConfig.engines.get(engineName)
        .map { cfg =>
          val url = cfg.endpoint.toUrl
          val path = s"$url?request=$request"
          log.info(s">>> $path")
          val s: String = Http(path).asString.body

          log.info(s"<<< $s")
          Response.success(s.parseJson.convertTo[SearchResponse])
        }.getOrElse {
        Response.fromThrowable(UnsupportedEngine(engineName))
      }
    }.recover {
      case e => Response.fromThrowable(e)
    }

  override def engines: Seq[String] = enginesConfig.engines.keys.toSeq
}
