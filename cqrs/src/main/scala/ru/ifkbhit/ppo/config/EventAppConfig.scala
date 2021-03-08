package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps
import ru.ifkbhit.ppo.common.model.config.{ActorSystemConfig, ApiConfig}

case class EventAppConfig(api: ApiConfig, actorSystemConfig: ActorSystemConfig)

object EventAppConfig extends ConfigBuilder[EventAppConfig] {
  override def apply(implicit config: Config): EventAppConfig =
    new EventAppConfig(
      api = ApiConfig(cfg"api"),
      actorSystemConfig = ActorSystemConfig(cfg"actor-system")
    )
}
