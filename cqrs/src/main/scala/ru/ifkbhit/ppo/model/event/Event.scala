package ru.ifkbhit.ppo.model.event

import java.sql.ResultSet

import org.joda.time.DateTime
import ru.ifkbhit.ppo.util.DateTimeUtils._
import spray.json.DefaultJsonProtocol._
import spray.json._


case class Event(
  eventId: Long,
  aggregateId: Long,
  eventType: EventType,
  data: JsValue,
  eventTime: DateTime
) {

  def payloadAs[T: JsonFormat]: T =
    data.convertTo[T]

}

object Event {

  implicit val format: JsonFormat[Event] = jsonFormat5(Event.apply)

  def fromRs(resultSet: ResultSet): Event = {
    Event(
      eventId = resultSet.getLong("event_id"),
      aggregateId = resultSet.getLong("aggregate_id"),
      eventType = EventType.withName(resultSet.getString("event_type")),
      data = resultSet.getString("data").parseJson,
      eventTime = new DateTime(resultSet.getTimestamp("event_time"))
    )
  }
}