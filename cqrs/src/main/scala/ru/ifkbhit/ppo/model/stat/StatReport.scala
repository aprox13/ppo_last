package ru.ifkbhit.ppo.model.stat

import ru.ifkbhit.ppo.model.event.Interval
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class StatReport(
  totalCount: Int,
  frequency: FrequencyReport,
  averageSpentMinutes: Int,
  interval: Interval
)

object StatReport {
  implicit val format: RootJsonFormat[StatReport] = jsonFormat4(StatReport.apply)
}
