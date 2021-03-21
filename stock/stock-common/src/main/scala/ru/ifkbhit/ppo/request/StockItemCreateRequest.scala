package ru.ifkbhit.ppo.request

import ru.ifkbhit.ppo.common.model.JsonFormatSupport
import ru.ifkbhit.ppo.model.Money
import spray.json.DefaultJsonProtocol._

case class StockItemCreateRequest(
  name: String,
  price: Money,
  count: Long
)

object StockItemCreateRequest extends JsonFormatSupport[StockItemCreateRequest](jsonFormat3(new StockItemCreateRequest(_, _, _)))

