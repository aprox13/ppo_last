package ru.ifkbhit.ppo.common.model.config

import com.typesafe.config.Config

import scala.concurrent.duration.Duration
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class ApiConfig(
  endpoint: HttpEndpoint,
  unbindTimeout: Duration
)

object ApiConfig {
  import scala.concurrent.duration._

  private val DefaultUnbindTimeout = 100.millis

  def apply(config: Config): ApiConfig = {
    implicit val c: Config = config
    new ApiConfig(
      endpoint = HttpEndpoint(cfg"endpoint"),
      unbindTimeout = optdur"unbind-timeout".getOrElse(DefaultUnbindTimeout)
    )
  }
}

