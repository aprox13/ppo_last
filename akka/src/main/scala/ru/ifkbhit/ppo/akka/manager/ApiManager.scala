package ru.ifkbhit.ppo.akka.manager

import ru.ifkbhit.ppo.akka.model.ApiResponse
import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.{ExecutionContext, Future}

trait ApiManager {

  def searchRequest(request: String)(implicit ec: ExecutionContext): Future[Response[ApiResponse]]
}
