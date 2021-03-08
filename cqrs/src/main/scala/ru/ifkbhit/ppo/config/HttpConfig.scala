package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps

case class HttpConfig(port: Int)

object HttpConfig extends ConfigBuilder[HttpConfig] {
  override def apply(implicit config: Config): HttpConfig =
    new HttpConfig(
      port = int"port"
    )
}