package ru.ifkbhit.ppo.akka.manager

import ru.ifkbhit.ppo.akka.manager.ApiManager.ApiResponse
import ru.ifkbhit.ppo.common.model.response.Response
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

import scala.concurrent.{ExecutionContext, Future}

trait ApiManager {

  def searchRequest(request: String)(implicit ec: ExecutionContext): Future[ApiResponse]
}

object ApiManager {
  type ApiResponse = Map[String, Response]

  implicit val format: JsonFormat[ApiResponse] =
    mapFormat[String, Response](implicitly, Response.WriteOnlyJsonFormat)
}