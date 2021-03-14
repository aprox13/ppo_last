package ru.ifkbhit.ppo.actions

import java.sql.{Connection, PreparedStatement, Statement}

import ru.ifkbhit.ppo.actions.EventActions.GetEvents
import ru.ifkbhit.ppo.actions.Filter.{SqlFilter, eventTypeInFilter, intervalFilter, longFilter}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.model.event.{Event, EventType, Interval, OpenInterval}
import ru.ifkbhit.ppo.util.DateTimeUtils.DateTimeOps
import ru.ifkbhit.ppo.util.SqlUtils.parseResultSet
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json.JsValue

trait EventActions extends Logging {

  def find(
    request: GetEvents
  ): DbAction[Seq[Event]]

  def insertOne(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Long]

  def getLastEventOf(eventTypes: Seq[EventType], aggregateId: Option[Long]): DbAction[Option[Event]] =
    find(
      GetEvents(
        eventTypes = Some(eventTypes),
        aggregateId = aggregateId,
        timeSorting = Some(Sorting.Desc),
        limit = Some(1)
      )
    ).map(_.headOption)

  def getLastEvent(eventType: EventType, aggregateId: Option[Long]): DbAction[Option[Event]] =
    getLastEventOf(Seq(eventType), aggregateId)

  def getOne(eventId: Long): DbAction[Event] = {
    for {
      eventOpt <- find(GetEvents(eventId = Some(eventId), limit = Some(1)))
      if eventOpt.nonEmpty
    } yield eventOpt.head
  }

  def insertAndReturn(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Event] =
    insertOne(eventType, payload, aggregatedId).flatMap(getOne)
}

object EventActions {

  case class GetEvents(
    eventId: Option[Long],
    eventTypes: Option[Seq[EventType]],
    aggregateId: Option[Long],
    eventTime: Interval = OpenInterval,
    timeSorting: Option[Sorting],
    limit: Option[Int]
  ) extends Logging {
    private[actions] def preparedStatement(conn: Connection) = {
      val eventsFilter: Option[SqlFilter] = eventTypes.flatMap(eventTypeInFilter)
      val aggregateIdFilter: Option[SqlFilter] = aggregateId.map(longFilter("aggregate_id"))

      val eventIdFilter = eventId.map(longFilter("event_id"))
      val eventTimeFilter = intervalFilter(eventTime)

      val filters: Seq[SqlFilter] = Seq(
        eventIdFilter,
        aggregateIdFilter,
        eventsFilter,
        eventTimeFilter
      ).flatten
      val filtersSql = filters.map(_.sql).mkString(" and ")
        .applyTransformIfPred(_.nonEmpty) {
          sql => s"where $sql"
        }
        .applyTransformIfPred(_.isEmpty) {
          _ => "1 = 1"
        }

      val order = timeSorting.map { s =>
        s"order by event_time ${s.entryName}"
      }
      val limitSql = limit.map { l =>
        s"limit $l"
      }

      val sql =
        s"""
           |select *
           |from events
           |$filtersSql
           |$order
           |$limitSql
           |""".stripMargin

      val ps = conn.prepareStatement(sql)

      filters.flatMap(_.setters)
        .zipWithIndex
        .foreach {
          case (setter, i) =>
            setter(ps, i + 1)
        }

      log.info(s"Got sql $sql")
      ps
    }
  }

  object GetEvents {
    def apply(
      eventId: Option[Long] = None,
      eventTypes: Option[Seq[EventType]] = None,
      aggregateId: Option[Long] = None,
      eventTime: Interval = OpenInterval,
      timeSorting: Option[Sorting] = None,
      limit: Option[Int] = None
    ): GetEvents =
      new GetEvents(eventId, eventTypes, aggregateId, eventTime, timeSorting, limit)
  }


}

class DefaultEventActions(implicit timeProvider: TimeProvider) extends EventActions {
  private type ArgSetter = (PreparedStatement, Int) => Unit

  override def insertOne(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Long] =
    DbAction { conn =>

      val ps = conn.prepareStatement(
        s"""
              insert into events (aggregate_id, event_type, data, event_time)
              values ($aggregatedId, '${eventType.entryName}', '${payload.compactPrint}', '${timeProvider.now().asPsqlTimestamp}')
              """,
        Statement.RETURN_GENERATED_KEYS
      )

      ps.executeUpdate().ensuring(_ != 0, "Couldn't insert new event")

      parseResultSet(ps.getGeneratedKeys)(_.getLong(1)).head
    }

  override def find(
    request: GetEvents
  ): DbAction[Seq[Event]] = DbAction { conn =>

    parseResultSet(request.preparedStatement(conn).executeQuery()) {
      Event.fromRs
    }
  }
}