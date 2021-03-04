package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class AppConfig(
  httpConfig: HttpConfig,
  mongoConfig: MongoConfig,
  schedulerConfig: SchedulerConfig,
  mongoCollections: MongoCollections,
  currencyApiUrl: String
)

object AppConfig extends ConfigBuilder[AppConfig] {

  override def apply(implicit config: Config): AppConfig =
    new AppConfig(
      httpConfig = HttpConfig(cfg"http"),
      mongoConfig = MongoConfig(cfg"mongo"),
      schedulerConfig = SchedulerConfig(cfg"scheduler"),
      mongoCollections = MongoCollections(cfg"mongo-collections"),
      currencyApiUrl = str"currencies.api.url"
    )
}
