package ru.ifkbhit.ppo.akka.util

import ru.ifkbhit.ppo.akka.config.EnginesConfig
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.manager.impl.SearchManagerImpl
import ru.ifkbhit.ppo.common.model.config.{ApiConfig, HttpEndpoint}

import scala.concurrent.duration.DurationInt

object TestSearcherClientProvider {

  val EnginesCfg: EnginesConfig = EnginesConfig(
    Map(
      "test1" -> ApiConfig(
        endpoint = HttpEndpoint(
          "localhost",
          12345,
          "http"
        ),
        unbindTimeout = 1.second
      ),
      "test2" -> ApiConfig(
        endpoint = HttpEndpoint(
          "localhost",
          12346,
          "http"
        ),
        unbindTimeout = 1.second
      ),
      "test3" -> ApiConfig(
        endpoint = HttpEndpoint(
          "localhost",
          12347,
          "http"
        ),
        unbindTimeout = 1.second
      )
    )
  )


}
