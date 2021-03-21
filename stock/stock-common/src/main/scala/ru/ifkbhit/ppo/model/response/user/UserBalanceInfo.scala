package ru.ifkbhit.ppo.model.response.user

import ru.ifkbhit.ppo.model.Money
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserBalanceInfo(
  balance: Money,
  atStocks: Money,
  total: Money
)

object UserBalanceInfo {
  implicit val format: RootJsonFormat[UserBalanceInfo] = jsonFormat3(UserBalanceInfo.apply)
}