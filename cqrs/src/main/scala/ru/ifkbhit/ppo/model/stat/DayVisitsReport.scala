package ru.ifkbhit.ppo.model.stat

import org.joda.time.DateTime
import ru.ifkbhit.ppo.model.event.ClosedInterval
import ru.ifkbhit.ppo.util.DateTimeUtils.{DateTimeFormat, _}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.math.Ordering.Implicits._


case class DayVisitsReport(
  date: DateTime,
  visits: Seq[OneVisitReport],
  count: Int
)

object DayVisitsReport {
  implicit val format: RootJsonFormat[DayVisitsReport] = jsonFormat3(DayVisitsReport.apply)

  def build(visitIntervals: Seq[ClosedInterval]): Option[DayVisitsReport] = {
    visitIntervals.headOption.map { firstVisit =>
      require(
        visitIntervals.forall(_.from.withTimeAtStartOfDay() equiv firstVisit.from.withTimeAtStartOfDay()),
        "Visits must be in one day"
      )

      val visits = visitIntervals.map { v =>
        OneVisitReport(
          enterTime = v.from,
          minutes = v.duration.toMinutes.toInt
        )
      }

      DayVisitsReport(
        firstVisit.from.withTimeAtStartOfDay(),
        visits = visits,
        count = visits.size
      )
    }
  }
}
