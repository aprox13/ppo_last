package ru.ifkbhit.ppo.utils

import java.util.concurrent.atomic.AtomicReference

import ru.ifkbhit.ppo.actions.{DbAction, EventActions}
import ru.ifkbhit.ppo.model.event.{Event, EventType}
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json.JsValue

import scala.collection.mutable.ArrayBuffer

class MemoryEventActions(database: AtomicReference[ArrayBuffer[Event]])(implicit timeProvider: TimeProvider) extends EventActions {
  override def getLastEventOf(eventTypes: Seq[EventType], aggregateId: Option[Long]): DbAction[Option[Event]] =
    DbAction.success {
      database.get()
        .sortBy(_.eventTime.getMillis)
        .reverse
        .find { event =>
          eventTypes.contains(event.eventType) && aggregateId.forall(_ == event.aggregateId)
        }
    }

  override def insertOne(eventType: EventType, payload: JsValue, aggregatedId: Long): DbAction[Long] = DbAction.success {
    val newId = database.get()
      .sortBy(_.eventTime.getMillis)
      .lastOption.map(_.eventId).getOrElse(0L) + 1L

    val event = Event(
      newId,
      aggregatedId,
      eventType,
      payload,
      timeProvider.now()
    )

    database.get() += event

    newId
  }

  override def getOne(eventId: Long): DbAction[Event] =
    DbAction.success {
      database.get().find(_.eventId == eventId)
        .ensuring(_.nonEmpty, s"Couldn't find event by id $eventId")
        .head
    }
}
