package ru.ifkbhit.ppo.model.request

import spray.json._
import spray.json.DefaultJsonProtocol._

case class ProductRequest(
  id: Long,
  userId: Option[Long]
)

object ProductRequest {
  implicit val reader: JsonReader[ProductRequest] = jsonFormat2(ProductRequest.apply)
}
