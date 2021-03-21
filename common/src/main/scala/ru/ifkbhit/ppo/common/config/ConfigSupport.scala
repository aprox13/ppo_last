package ru.ifkbhit.ppo.common.config

import com.typesafe.config.{Config, ConfigFactory, ConfigResolveOptions}

trait ConfigSupport {
  lazy val currentConfig: Config =
    ConfigFactory.parseResources("application.conf")
      .resolve(ConfigResolveOptions.defaults())

  implicit lazy val implicitCurrentConfig: Config = currentConfig
}
