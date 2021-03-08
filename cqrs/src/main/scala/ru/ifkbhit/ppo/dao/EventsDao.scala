package ru.ifkbhit.ppo.dao

import ru.ifkbhit.ppo.model.event.{Event, EventType}
import spray.json.JsValue

import scala.concurrent.Future

trait EventsDao {

  def getLastEvent(eventType: EventType, aggregateId: Long): Future[Option[Event]]

  def insertEvent(eventType: EventType, payload: JsValue, aggregatedId: Long): Future[Event]
}
