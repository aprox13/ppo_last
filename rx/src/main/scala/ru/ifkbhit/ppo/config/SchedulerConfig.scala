package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class SchedulerConfig(pools: Int)

object SchedulerConfig extends ConfigBuilder[SchedulerConfig] {
  override def apply(implicit config: Config): SchedulerConfig =
    SchedulerConfig(
      pools = int"pools"
    )
}