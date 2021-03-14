package ru.ifkbhit.ppo.model.stat

import spray.json.DefaultJsonProtocol._
import spray.json._

case class VisitReport(
  total: Int,
  perDays: Seq[DayVisitsReport]
)

object VisitReport {
  implicit val format: RootJsonFormat[VisitReport] = jsonFormat2(VisitReport.apply)

  def build(perDays: Iterable[DayVisitsReport]): VisitReport = {
    VisitReport(
      perDays.foldLeft(0)(_ + _.count),
      perDays.toSeq
    )
  }
}
