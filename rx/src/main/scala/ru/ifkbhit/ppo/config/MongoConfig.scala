package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class MongoConfig(
  host: String,
  port: Int,
  database: String
) {
  def connectionString: String = s"mongodb://$host:$port"
}

object MongoConfig extends ConfigBuilder[MongoConfig] {

  override def apply(implicit config: Config): MongoConfig =
    new MongoConfig(
      host = str"host",
      port = int"port",
      database = str"db-name"
    )
}
