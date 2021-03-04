package ru.ifkbhit.ppo.common.model.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class ActorSystemConfig(name: String, pools: Int)

object ActorSystemConfig {
  def apply(config: Config): ActorSystemConfig = {
    implicit val c: Config = config
    new ActorSystemConfig(
      name = str"name",
      pools = int"pools"
    )
  }
}