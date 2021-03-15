package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps

case class AppConfig(
  gate: EventAppConfig,
  managers: EventAppConfig,
  stat: EventAppConfig,
  eventStoreConfig: EventStoreConfig
)

object AppConfig extends ConfigBuilder[AppConfig] {
  override def apply(implicit config: Config): AppConfig =
    new AppConfig(
      gate = EventAppConfig(cfg"gate"),
      managers = EventAppConfig(cfg"managers"),
      stat = EventAppConfig(cfg"stat"),
      eventStoreConfig = EventStoreConfig(cfg"event-store")
    )
}
