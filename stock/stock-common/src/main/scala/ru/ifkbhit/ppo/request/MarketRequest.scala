package ru.ifkbhit.ppo.request

import ru.ifkbhit.ppo.common.model.JsonFormatSupport
import spray.json.DefaultJsonProtocol._

case class MarketRequest(userId: Long, stockId: Long, count: Long)

object MarketRequest
  extends JsonFormatSupport[MarketRequest](jsonFormat3(new MarketRequest(_, _, _)))