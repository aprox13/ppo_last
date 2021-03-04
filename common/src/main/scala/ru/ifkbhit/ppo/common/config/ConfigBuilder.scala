package ru.ifkbhit.ppo.common.config

import com.typesafe.config.Config

trait ConfigBuilder[T] {

  def apply(implicit config: Config): T
}
