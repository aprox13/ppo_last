package ru.ifkbhit.ppo.components

import ru.ifkbhit.ppo.common.model.config.HttpEndpoint
import ru.ifkbhit.ppo.utils.MarketHttpClient

trait MarketHttpClientToContainerComponents {

  self: MarketInContainerComponents =>

  val marketHttpClient = new MarketHttpClient(
    HttpEndpoint(
      host = "localhost",
      port = marketContainer.getMappedApiPort,
      schema = "http"
    )
  )
}
