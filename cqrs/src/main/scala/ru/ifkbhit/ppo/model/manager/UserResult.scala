package ru.ifkbhit.ppo.model.manager

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils._
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserResult(
  name: String,
  passExpire: Option[DateTime],
  isPassActive: Boolean
)

object UserResult {
  implicit val format: RootJsonFormat[UserResult] = jsonFormat3(UserResult.apply)

  def build(user: UserPayload, renewPassOpt: Option[RenewPassPayload]): UserResult = {
    UserResult(
      name = user.name,
      passExpire = renewPassOpt.map(_.expireAt),
      isPassActive = renewPassOpt.exists(_.expireAt.isAfterNow)
    )
  }


}
