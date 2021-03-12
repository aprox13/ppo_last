package ru.ifkbhit.ppo.actions

import java.sql.PreparedStatement

import ru.ifkbhit.ppo.model.event.EventType


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
}