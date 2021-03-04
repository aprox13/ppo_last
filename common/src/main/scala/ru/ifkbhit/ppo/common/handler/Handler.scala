package ru.ifkbhit.ppo.common.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait Handler {
  def route: Route
}

trait PingRoute extends Handler {

  private val pingRoute: Route = (get & path("ping")) {
    complete("pong")
  }

  abstract override def route: Route = super.route ~ pingRoute
}
