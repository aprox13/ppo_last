package ru.ifkbhit.ppo.actions

import java.sql.{PreparedStatement, Statement}

import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.dao.DbAction
import ru.ifkbhit.ppo.model.event.{Event, EventType}
import ru.ifkbhit.ppo.util.SqlUtils.parseResultSet
import spray.json.JsValue

trait EventActions extends Logging {
  def getLastEventOf(eventTypes: Seq[EventType], aggregateId: Option[Long]): DbAction[Option[Event]]

  def getLastEvent(eventType: EventType, aggregateId: Option[Long]): DbAction[Option[Event]] =
    getLastEventOf(Seq(eventType), aggregateId)

  def insertOne(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Long]

  def getOne(eventId: Long): DbAction[Event]

  def insertAndReturn(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Event] =
    insertOne(eventType, payload, aggregatedId).flatMap(getOne)
}

object DefaultEventActions extends EventActions {
  private type ArgSetter = (PreparedStatement, Int) => Unit

  private case class Filter(sql: String, setters: Seq[ArgSetter])

  override def getLastEventOf(eventTypes: Seq[EventType], aggregateId: Option[Long]): DbAction[Option[Event]] = DbAction { conn =>

    val eventsFilter: Option[Filter] = Some(eventTypes.map(_ => "cast(? as event_t)"))
      .filter(_.nonEmpty)
      .map { sql =>
        Filter(
          s"event_type in ${sql.mkString("(", ",", ")")}",
          eventTypes.map { e =>
            (ps: PreparedStatement, i: Int) => ps.setString(i, e.entryName)
          }
        )
      }

    val aggregateIdFilter: Option[Filter] = aggregateId.map {
      ai =>
        Filter("aggregate_id = ?", Seq(_.setLong(_, ai)))
    }


    val filters: Seq[Filter] = Seq(eventsFilter, aggregateIdFilter).flatten
    val filtersSql = filters.map(_.sql).mkString(" and ")

    val sql =
      s"""
         |select *
         |from events
         |where $filtersSql
         |order by event_time desc
         |limit 1
         |""".stripMargin

    log.info(s"Got sql $sql")
    val ps = conn.prepareStatement(sql)

    filters.flatMap(_.setters)
      .zipWithIndex
      .foreach {
        case (setter, i) =>
          setter(ps, i + 1)
      }

    parseResultSet(ps.executeQuery()) {
      Event.fromRs
    }.headOption
  }

  override def insertOne(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Long] =
    DbAction { conn =>
      val ps = conn.prepareStatement(
        s"""
              insert into events (aggregate_id, event_type, data)
              values ($aggregatedId, '${eventType.entryName}', '${payload.compactPrint}')
              """,
        Statement.RETURN_GENERATED_KEYS
      )

      ps.executeUpdate().ensuring(_ != 0, "Couldn't insert new event")

      parseResultSet(ps.getGeneratedKeys)(_.getLong(1)).head
    }

  override def getOne(eventId: Long): DbAction[Event] = DbAction { conn =>
    val select = conn.prepareStatement(s"select * from events where event_id = $eventId")

    parseResultSet(select.executeQuery()) {
      Event.fromRs
    }.ensuring(_.nonEmpty, s"Couldn't find event by id $eventId").head
  }

}