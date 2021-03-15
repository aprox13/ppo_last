package ru.ifkbhit.ppo.akka.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigOps._

import scala.concurrent.duration.FiniteDuration

case class ApiActorConfig(
  collectingTimeout: FiniteDuration
)

object ApiActorConfig {

  def apply(config: Config): ApiActorConfig = {
    implicit val c: Config = config

    new ApiActorConfig(
      collectingTimeout = findur"collecting-timeout"
    )
  }
}
