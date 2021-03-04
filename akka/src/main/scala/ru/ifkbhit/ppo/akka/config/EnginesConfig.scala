package ru.ifkbhit.ppo.akka.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.config.ApiConfig
import ru.ifkbhit.ppo.common.config.ConfigOps._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class EnginesConfig(engines: Map[String, ApiConfig])

object EnginesConfig extends Logging {

  private val EnginePrefix = "engine-"
  private val EngineDefPattern = ("^" + EnginePrefix + "(\\w+)\\.*").r

  def apply(config: Config): EnginesConfig = {
    implicit val c: Config = config
    new EnginesConfig(
      config.entrySet()
        .asScala
        .map(_.getKey)
        .map(key => key.split("\\.").head)
        .collect {
          case EngineDefPattern(engineName) =>
            engineName -> Try(ApiConfig(cfg"$EnginePrefix$engineName"))
          case x =>
            x -> Failure(new RuntimeException(x))
        }.map {
          case (name, Success(apiConfig)) =>
            name -> apiConfig
          case (name, Failure(exception)) =>
            log.error(s"Couldn't parse config for $EnginePrefix$name engine", exception)
            throw exception
        }.toMap
    )
  }
}