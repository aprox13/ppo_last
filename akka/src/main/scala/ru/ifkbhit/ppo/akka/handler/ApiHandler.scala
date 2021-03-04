package ru.ifkbhit.ppo.akka.handler

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.akka.manager.ApiManager
import ru.ifkbhit.ppo.common.handler.Handler

import scala.concurrent.ExecutionContext

class ApiHandler(apiManager: ApiManager)(implicit ec: ExecutionContext) extends Handler {

  override def route: Route = (get & parameter('request) ) {
    request =>
      complete(???)//apiManager.searchRequest(request))
  }

}
