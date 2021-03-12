package ru.ifkbhit.ppo.model.stat

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class UserStat(
  totalCount: Int,
  frequencyPerMonth: Float
)

object UserStat {
  implicit val format: RootJsonFormat[UserStat] = jsonFormat2(UserStat.apply)
}
