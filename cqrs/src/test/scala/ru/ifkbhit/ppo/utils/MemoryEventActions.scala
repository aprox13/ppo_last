package ru.ifkbhit.ppo.utils

import java.util.concurrent.atomic.AtomicReference

import ru.ifkbhit.ppo.actions.{DbAction, EventActions, Sorting}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.model.event.{Event, EventType}
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json.JsValue

import scala.collection.mutable.ArrayBuffer

class MemoryEventActions(database: AtomicReference[ArrayBuffer[Event]])(implicit timeProvider: TimeProvider) extends EventActions with Logging {

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

  override def find(request: EventActions.GetEvents): DbAction[Seq[Event]] =

    DbAction.success {
      database.get()
        .applyTransformIf(request.timeSorting.isDefined) { events =>
          events.sortBy(_.eventTime.getMillis)
            .applyTransformIf(request.timeSorting.contains(Sorting.Desc))(_.reverse)
        }
        .filter(e => request.eventTime.contains(e.eventTime))
        .filter(e => request.eventId.forall(_ == e.eventId))
        .filter(e => request.aggregateId.forall(_ == e.aggregateId))
        .filter(e => request.eventTypes.forall(_.contains(e.eventType)))
        .applyTransformIf(request.limit.isDefined) {
          _.take(request.limit.get)
        }
    }
}
