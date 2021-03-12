package ru.ifkbhit.ppo.model.stat

import org.joda.time.DateTime
import ru.ifkbhit.ppo.model.event.{Event, Interval}
import ru.ifkbhit.ppo.util.DateTimeUtils.DateTimeFormat
import spray.json.DefaultJsonProtocol._
import spray.json._


case class DayVisitReport(
  date: DateTime,
  times: Seq[DateTime],
  count: Int
)

object DayVisitReport {
  implicit val format: RootJsonFormat[DayVisitReport] = jsonFormat3(DayVisitReport.apply)

  def build(events: Seq[Event]): Option[DayVisitReport] = {
    Some(events).filter(_.nonEmpty).map { es =>

      val day = es.head.eventTime.withTimeAtStartOfDay()
      val dayInterval = Interval(
        from = Some(day),
        to = Some(day.plusDays(1))
      )

      require(es.forall(e => dayInterval.contains(e.eventTime)))

      DayVisitReport(
        day,
        es.map(_.eventTime),
        es.size
      )
    }
  }
}
