package ru.ifkbhit.ppo.model.response.user

import ru.ifkbhit.ppo.model.Money
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserStockInfo(
  name: String,
  count: Long,
  price: Money
)

object UserStockInfo {
  implicit val format: RootJsonFormat[UserStockInfo] = jsonFormat3(UserStockInfo.apply)
}
