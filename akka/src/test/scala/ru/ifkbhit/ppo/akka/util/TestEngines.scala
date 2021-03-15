package ru.ifkbhit.ppo.akka.util

import ru.ifkbhit.ppo.akka.config.EnginesConfig
import ru.ifkbhit.ppo.common.model.config.{ApiConfig, HttpEndpoint}

import scala.concurrent.duration.DurationInt

trait TestEngines {

  case class TestEngine(name: String, port: Int)

  def enginesConfig(engines: TestEngine*): EnginesConfig = EnginesConfig(
    engines.map { e =>
      e.name -> ApiConfig(
        endpoint = HttpEndpoint(
          "localhost",
          e.port,
          "http"
        ),
        unbindTimeout = 1.minute
      )

    }.toMap
  )

}
