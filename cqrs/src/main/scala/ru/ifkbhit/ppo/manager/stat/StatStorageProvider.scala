package ru.ifkbhit.ppo.manager.stat

import java.sql.Connection

import ru.ifkbhit.ppo.actions.{EventActions, Sorting}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.provider.Provider
import ru.ifkbhit.ppo.model.event.{ClosedInterval, Event, EventType}

class StatStorageProvider(connection: Connection, eventActions: EventActions)
  extends Provider[UserStatStorage]
    with Logging {

  override def get: UserStatStorage =
    new UserStatStorage(
      eventActions.find(
        EventActions.GetEvents(
          eventTypes = Some(Seq(EventType.UserEntered, EventType.UserExit)),
          timeSorting = Some(Sorting.Asc)
        )
      ).transactionalBlocked(connection)
        .groupBy(_.aggregateId)
        .mapValues(_.sortBy(_.eventTime.getMillis))
        .mapValues(buildVisitIntervals)
    )

  private def buildVisitIntervals(events: Seq[Event]): Seq[ClosedInterval] =
    events.dropWhile(_.eventType == EventType.UserExit)
      .foldLeft((Seq.empty[ClosedInterval], Option.empty[Event])) {
        case ((result, Some(lastEnter)), Event(_, _, EventType.UserExit, _, exitTime))
          if lastEnter.eventTime.isBefore(exitTime) =>
          (result :+ ClosedInterval(lastEnter.eventTime, exitTime), None)
        case ((result, None), enterEvent@Event(_, _, EventType.UserEntered, _, _)) =>
          (result, Some(enterEvent))
        case ((_, lastEnterEvent), nextEvent) =>
          log.error(s"Inconsistent state: last enter event: $lastEnterEvent, nextEvent: $nextEvent")
          throw new RuntimeException("Couldn't build visits intervals")
      }._1
}
