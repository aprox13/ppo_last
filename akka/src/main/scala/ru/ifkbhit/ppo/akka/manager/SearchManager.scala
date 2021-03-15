package ru.ifkbhit.ppo.akka.manager

import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.{ExecutionContext, Future}

trait SearchManager {

  def searchFrom(engineName: String, request: String)(implicit ec: ExecutionContext): Future[Response]

  def engines: Seq[String]

  def enginesCount: Int = engines.size
}

object SearchManager {

  case class UnsupportedEngine(name: String) extends RuntimeException(s"Unsupported engine $name")

}
