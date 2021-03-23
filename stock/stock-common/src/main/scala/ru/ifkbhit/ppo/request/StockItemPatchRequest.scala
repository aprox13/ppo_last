package ru.ifkbhit.ppo.request

import ru.ifkbhit.ppo.common.model.JsonFormatSupport
import spray.json.DefaultJsonProtocol._


case class StockItemPatchRequest(
  id: Long,
  patches: Seq[StockItemPatch]
)

object StockItemPatchRequest extends JsonFormatSupport[StockItemPatchRequest](jsonFormat2(new StockItemPatchRequest(_, _)))
