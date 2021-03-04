package ru.ifkbhit.ppo.common.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigSupport {
  lazy val currentConfig: Config =
    ConfigFactory.parseResources("application.conf")

  implicit lazy val implicitCurrentConfig: Config = currentConfig
}
