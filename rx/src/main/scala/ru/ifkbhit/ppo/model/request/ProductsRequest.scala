package ru.ifkbhit.ppo.model.request
import spray.json._
import spray.json.DefaultJsonProtocol._

case class ProductsRequest(
  userId: Option[Long]
)

object ProductsRequest {
  implicit val reader: JsonReader[ProductsRequest] = jsonFormat1(ProductsRequest.apply)
}
