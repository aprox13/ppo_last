package ru.ifkbhit.ppo.akka.config

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class ApiActorConfig(
  fullTimeout: FiniteDuration,
  collectingTimeout: FiniteDuration
)

object ApiActorConfig {

  def apply(config: Config): ApiActorConfig = {
    implicit val c: Config = config

    new ApiActorConfig(
      fullTimeout = findur"full-timeout",
      collectingTimeout = findur"collecting-timeout"
    )
  }
}
