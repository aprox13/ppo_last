package ru.ifkbhit.ppo.akka.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.akka.manager.ApiManager
import ru.ifkbhit.ppo.akka.manager.ApiManager.format
import ru.ifkbhit.ppo.common.handler.JsonAnsweredHandler

import scala.concurrent.ExecutionContext


class ApiHandler(apiManager: ApiManager)(implicit val ec: ExecutionContext) extends JsonAnsweredHandler {
  override def route: Route = (get & path("search") & parameter('request)) {
    request =>
      completeResponse(apiManager.searchRequest(request))
  }

}
