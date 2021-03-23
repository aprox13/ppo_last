package ru.ifkbhit.ppo.common.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import ru.ifkbhit.ppo.common.model.response.Response
import spray.json.{DefaultJsonProtocol, JsonFormat}

import scala.concurrent.{ExecutionContext, Future}

trait Handler {
  def route: Route
}

trait PingRoute extends Handler {

  private val pingRoute: Route = (get & path("ping")) {
    complete("pong")
  }

  abstract override def route: Route = pingRoute ~ super.route
}

trait JsonAnsweredHandler extends Handler with SprayJsonSupport with DefaultJsonProtocol {

  protected implicit val ec: ExecutionContext

  protected def completeResponse[T: JsonFormat](f: => Future[T]): StandardRoute = {
    complete {
      val res = f
      res.map(Response.success[T])
        .recover {
          case e => Response.fromThrowable(e)
        }
    }
  }
}