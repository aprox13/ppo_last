package ru.ifkbhit.ppo.model.response.user

import ru.ifkbhit.ppo.model.{Money, User}
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserInfo(
  id: Long,
  name: String,
  balance: UserBalanceInfo,
  stocks: Seq[UserStockInfo]
)

object UserInfo {

  implicit val format: RootJsonFormat[UserInfo] = jsonFormat4(UserInfo.apply)

  def build(user: User, stocks: Seq[UserStockInfo]): UserInfo = {
    val balance = stocks.foldLeft(UserBalanceInfo(balance = user.balance, atStocks = Money.NoMoney, total = user.balance)) {
      case (result, stockInfo) =>
        val delta = stockInfo.price * stockInfo.count

        result.copy(
          atStocks = result.atStocks + delta,
          total = result.total + delta
        )
    }
    UserInfo(
      user.id.getOrElse(throw new RuntimeException("Unexpected empty user id")),
      user.name,
      balance,
      stocks
    )
  }
}
