package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps
import ru.ifkbhit.ppo.common.model.config.{ActorSystemConfig, ApiConfig}
import ru.ifkbhit.ppo.database.provider.DbConfig

case class AppConfig(
  api: ApiConfig,
  actorSystemConfig: ActorSystemConfig,
  dbConfig: DbConfig
)

object AppConfig extends ConfigBuilder[AppConfig] {
  override def apply(implicit config: Config): AppConfig =
    new AppConfig(
      api = ApiConfig(cfg"api"),
      actorSystemConfig = ActorSystemConfig(cfg"akka"),
      dbConfig = DbConfig(cfg"database")
    )
}