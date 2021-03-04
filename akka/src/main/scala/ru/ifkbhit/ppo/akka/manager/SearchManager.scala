package ru.ifkbhit.ppo.akka.manager

import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.{ExecutionContext, Future}

trait SearchManager {

  def searchFrom(engineName: String, request: String): Response[SearchResponse]
  def engines: Seq[String]
  def enginesCount: Int = engines.size
}
