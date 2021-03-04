package ru.ifkbhit.ppo.model.response

import ru.ifkbhit.ppo.model.StoredProduct
import spray.json._
import spray.json.DefaultJsonProtocol._

case class ProductResponse(
  id: Long,
  name: String,
  price: String
)

object ProductResponse {
  implicit val jsonFormat: JsonFormat[ProductResponse] = jsonFormat3(ProductResponse.apply)

  def fromStored(stored: StoredProduct): ProductResponse = {
    ProductResponse(
      id = stored.id,
      name = stored.name,
      price = stored.price.prettyPrint
    )


  }

}