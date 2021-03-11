package ru.ifkbhit.ppo.model.manager

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils._
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserResult(
  id: Long,
  name: String,
  passExpire: Option[DateTime],
  isPassActive: Boolean
)

object UserResult {
  implicit val format: RootJsonFormat[UserResult] = jsonFormat4(UserResult.apply)

  def build(
    id: Long,
    user: UserPayload,
    renewPassOpt: Option[RenewPassPayload]
  )(implicit timeProvider: TimeProvider): UserResult = {
    UserResult(
      id = id,
      name = user.name,
      passExpire = renewPassOpt.map(_.expireAt),
      isPassActive = renewPassOpt.exists(_.expireAt.isAfter(timeProvider.now()))
    )
  }


}
