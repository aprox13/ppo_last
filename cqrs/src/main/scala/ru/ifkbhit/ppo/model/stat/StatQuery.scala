package ru.ifkbhit.ppo.model.stat

import ru.ifkbhit.ppo.model.event.Interval
import spray.json.DefaultJsonProtocol._
import spray.json._

case class StatQuery(
  userId: Long,
  interval: Interval,
  frequency: FrequencyReport.Frequency
)

object StatQuery {
  implicit val format: RootJsonFormat[StatQuery] = jsonFormat3(StatQuery.apply)
}
