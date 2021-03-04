package ru.ifkbhit.ppo.akka.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigOps._
import ru.ifkbhit.ppo.common.model.config.{ActorSystemConfig, ApiConfig}

case class AppConfig(
  apiConfig: ApiConfig,
  enginesConfig: EnginesConfig,
  actorSystemConfig: ActorSystemConfig,
  apiActorConfig: ApiActorConfig
)

object AppConfig {

  def provide(implicit config: Config): AppConfig =
    AppConfig(
      ApiConfig(cfg"api"),
      enginesConfig = EnginesConfig(cfg"engines"),
      actorSystemConfig = ActorSystemConfig(cfg"actor-system"),
      apiActorConfig = ApiActorConfig(cfg"api-actor")
    )
}