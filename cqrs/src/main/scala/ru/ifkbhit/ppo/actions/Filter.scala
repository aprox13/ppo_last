package ru.ifkbhit.ppo.actions

import java.sql.PreparedStatement

import org.joda.time.DateTime
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.model.event._
import ru.ifkbhit.ppo.util.DateTimeUtils._

object Filter {
  private type ArgSetter = (PreparedStatement, Int) => Unit

  case class SqlFilter(sql: String, setters: Seq[ArgSetter])

  def longFilter(name: String)(l: Long): SqlFilter = SqlFilter(name + " = ?", Seq(_.setLong(_, l)))

  def aggregateIdFilter(id: Long): SqlFilter = SqlFilter("aggregate_id = ?", Seq(_.setLong(_, id)))

  def eventTypeInFilter(eventTypes: EventType*): Option[SqlFilter] = {
    Some(eventTypes.map(_ => "cast(? as event_t)"))
      .filter(_.nonEmpty)
      .map { sql =>
        SqlFilter(
          s"event_type in ${sql.mkString("(", ",", ")")}",
          eventTypes.map { e =>
            (ps: PreparedStatement, i: Int) => ps.setString(i, e.entryName)
          }
        )
      }
  }


  def intervalFilter(interval: Interval): Option[SqlFilter] = {
    def psqlTs(dateTime: DateTime): ArgSetter =
      _.setString(_, dateTime.asPsqlTimestamp)

    interval collect {
      case FromInterval(from) =>
        SqlFilter(
          "event_time >= ?",
          Seq(psqlTs(from))
        )
      case ToInterval(to) =>
        SqlFilter(
          "event_time <= ?",
          Seq(psqlTs(to))
        )
      case ClosedInterval(from, to) =>
        SqlFilter(
          "event_time >= ? and event_time <= ?",
          Seq(psqlTs(from), psqlTs(to))
        )
    }


  }
}