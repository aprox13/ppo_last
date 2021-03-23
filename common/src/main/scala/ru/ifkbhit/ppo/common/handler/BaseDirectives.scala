package ru.ifkbhit.ppo.common.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1}

trait BaseDirectives {
  val UserInPath: Directive1[Long] = pathPrefix("user" / LongNumber)
  val Command: Directive0 = pathPrefix("cmd")
  val Query: Directive0 = pathPrefix("query")
}

object BaseDirectives extends BaseDirectives
