package ru.ifkbhit.ppo.model.manager

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json.DefaultJsonProtocol._
import spray.json._

case class RenewPassPayload(
  expireAt: DateTime
)

object RenewPassPayload {

  import ru.ifkbhit.ppo.util.DateTimeUtils._

  implicit val format: JsonFormat[RenewPassPayload] = jsonFormat1(RenewPassPayload.apply)

  def today(implicit timeProvider: TimeProvider): RenewPassPayload = RenewPassPayload(timeProvider.now().withTimeAtStartOfDay())
}
