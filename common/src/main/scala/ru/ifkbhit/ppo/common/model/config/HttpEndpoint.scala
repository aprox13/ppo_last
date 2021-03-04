package ru.ifkbhit.ppo.common.model.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class HttpEndpoint(
  host: String,
  port: Int,
  schema: String
) {
  def toUrl: String =
    s"$schema://$host:$port"
}

object HttpEndpoint {
  private val DefaultPort: Int = 80

  def apply(config: Config): HttpEndpoint = {
    implicit val c: Config = config

    new HttpEndpoint(
      host = str"host",
      port = opti"port".getOrElse(DefaultPort),
      schema = opts"schema".getOrElse("http")
    )
  }
}