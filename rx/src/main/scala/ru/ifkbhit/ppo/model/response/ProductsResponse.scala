package ru.ifkbhit.ppo.model.response

import ru.ifkbhit.ppo.model.StoredProduct
import spray.json.DefaultJsonProtocol._
import spray.json._

case class ProductsResponse(
  products: Seq[ProductResponse]
)

object ProductsResponse {
  implicit val JsonFormat: JsonFormat[ProductsResponse] = jsonFormat1(ProductsResponse.apply)
}