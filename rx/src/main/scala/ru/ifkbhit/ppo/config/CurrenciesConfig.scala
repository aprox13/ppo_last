package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps._

import scala.concurrent.duration.FiniteDuration

case class CurrenciesConfig(
  apiUrl: String,
  updateEvery: FiniteDuration
)

object CurrenciesConfig extends ConfigBuilder[CurrenciesConfig] {
  override def apply(implicit config: Config): CurrenciesConfig =
    new CurrenciesConfig(
      apiUrl = str"api.url",
      updateEvery = findur"update-every"
    )
}
