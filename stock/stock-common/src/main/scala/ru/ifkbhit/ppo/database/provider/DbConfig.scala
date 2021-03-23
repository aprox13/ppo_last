package ru.ifkbhit.ppo.database.provider

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps.ConfigInterpStringOps
import ru.ifkbhit.ppo.common.model.config.HttpEndpoint

case class DbConfig(
  name: String,
  endpoint: HttpEndpoint,
  user: String,
  password: Option[String],
  maxConnections: Option[Int],
  createSchema: Boolean
) {


  def connectionString: String =
    s"${endpoint.toUrl}/$name"
}

object DbConfig extends ConfigBuilder[DbConfig] {
  override def apply(implicit config: Config): DbConfig =
    new DbConfig(
      name = str"name",
      endpoint = HttpEndpoint(cfg"endpoint"),
      user = str"user",
      password = opts"password",
      maxConnections = opti"max-connections",
      createSchema = bool"create-schema"
    )
}
