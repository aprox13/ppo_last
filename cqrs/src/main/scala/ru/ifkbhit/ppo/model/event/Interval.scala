package ru.ifkbhit.ppo.model.event

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.duration.{FiniteDuration, _}
import scala.math.Ordering.Implicits._

sealed abstract class Interval {

  def contains(dt: DateTime): Boolean =
    this match {
      case FromInterval(from) =>
        from <= dt
      case ToInterval(to) =>
        to <= dt
      case ClosedInterval(from, to) =>
        from <= dt && dt <= to
      case OpenInterval =>
        true
    }

  def intersect(another: Interval): Option[Interval] = {
    Some(this -> another) collectFirst {
      case (FromInterval(f), FromInterval(f1)) =>
        FromInterval(f `max` f1)
      case (ToInterval(t), ToInterval(t1)) =>
        ToInterval(t `min` t1)

      case (OpenInterval, x) => x
      case (x, OpenInterval) => x

      case (ClosedInterval(f, t), ClosedInterval(f1, t1))
        if t >= f1 || t1 >= f =>
        ClosedInterval(f `max` f1, t `min` t1)

      case (FromInterval(f), ToInterval(t))
        if f <= t =>
        ClosedInterval(f, t)
      case (ToInterval(t), FromInterval(f))
        if f <= t =>
        ClosedInterval(f, t)
    }
  }
}


case class FromInterval(from: DateTime) extends Interval

case class ToInterval(to: DateTime) extends Interval

case class ClosedInterval(from: DateTime, to: DateTime) extends Interval {
  def duration: FiniteDuration =
    (to.getMillis - from.getMillis).millis
}

case object OpenInterval extends Interval


object Interval {

  private case class MaybeBoundInterval(
    from: Option[DateTime],
    to: Option[DateTime]
  )

  private def maybeBound(from: DateTime = null, to: DateTime = null) =
    MaybeBoundInterval(Option(from), Option(to))

  private implicit val maybeBoundFormat: RootJsonFormat[MaybeBoundInterval] = jsonFormat2(MaybeBoundInterval.apply)
  implicit val format: RootJsonFormat[Interval] = new RootJsonFormat[Interval] {
    override def read(json: JsValue): Interval =
      maybeBoundFormat.read(json) match {
        case MaybeBoundInterval(Some(from), None) =>
          FromInterval(from)
        case MaybeBoundInterval(None, Some(to)) =>
          ToInterval(to)
        case MaybeBoundInterval(Some(from), Some(to)) =>
          ClosedInterval(from, to)
        case _ =>
          OpenInterval
      }

    override def write(obj: Interval): JsValue = {
      maybeBoundFormat.write(obj match {
        case FromInterval(from) =>
          maybeBound(from = from)
        case ToInterval(to) =>
          maybeBound(to = to)
        case ClosedInterval(from, to) =>
          maybeBound(from, to)
        case OpenInterval =>
          maybeBound()
      }
      )
    }
  }
}
