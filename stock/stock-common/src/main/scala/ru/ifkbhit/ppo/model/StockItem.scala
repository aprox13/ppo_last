package ru.ifkbhit.ppo.model

import ru.ifkbhit.ppo.request.StockItemCreateRequest
import spray.json.DefaultJsonProtocol._
import spray.json._

case class StockItem(
  id: Option[Long],
  name: String,
  price: Money,
  count: Long
)

object StockItem {

  def tupled: ((Long, String, Money, Long)) => StockItem = {
    case (a, b, c, d) => StockItem(Some(a), b, c, d)
  }

  def createForInsert(createRequest: StockItemCreateRequest): StockItem = {
    StockItem(
      id = None,
      name = createRequest.name,
      price = createRequest.price,
      count = createRequest.count
    )
  }

  implicit val format: RootJsonFormat[StockItem] = jsonFormat4(StockItem.apply)
}

