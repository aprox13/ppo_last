package ru.ifkbhit.ppo.model.event

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils.DateTimeFormat
import spray.json.DefaultJsonProtocol._
import spray.json._


case class Interval(
  from: Option[DateTime],
  to: Option[DateTime]
) {
  def contains(dt: DateTime): Boolean =
    from.forall(t => t.isBefore(dt) || t.isEqual(dt)) &&
      to.forall(t => t.isAfter(dt) || t.isEqual(dt))
}

object Interval {
  implicit val format: RootJsonFormat[Interval] = jsonFormat2(Interval.apply)

  val Open: Interval = Interval(None, None)
}
