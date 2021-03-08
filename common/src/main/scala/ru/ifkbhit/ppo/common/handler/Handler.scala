package ru.ifkbhit.ppo.common.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol

trait Handler {
  def route: Route
}

trait PingRoute extends Handler {

  private val pingRoute: Route = (get & path("ping")) {
    complete("pong")
  }

  abstract override def route: Route = super.route ~ pingRoute
}

trait JsonAnsweredHandler extends Handler with SprayJsonSupport with DefaultJsonProtocol