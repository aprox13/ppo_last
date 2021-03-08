package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps

case class EventStoreConfig(connectionString: String)

object EventStoreConfig extends ConfigBuilder[EventStoreConfig] {
  override def apply(implicit config: Config): EventStoreConfig =
    new EventStoreConfig(
      connectionString = str"connection-string"
    )
}
