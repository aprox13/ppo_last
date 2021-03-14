package ru.ifkbhit.ppo.model.stat

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils.DateTimeFormat
import spray.json.DefaultJsonProtocol._
import spray.json._

case class OneVisitReport(
  enterTime: DateTime,
  minutes: Int
)

object OneVisitReport {

  implicit val format: RootJsonFormat[OneVisitReport] = jsonFormat2(OneVisitReport.apply)
}