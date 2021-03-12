package ru.ifkbhit.ppo.manager.impl

import java.sql.Connection

import ru.ifkbhit.ppo.actions.EventActions
import ru.ifkbhit.ppo.manager.StatManager
import ru.ifkbhit.ppo.model.event.{EventType, Interval}
import ru.ifkbhit.ppo.model.stat.{DayVisitReport, PerDayReportQuery, VisitReport}

import scala.concurrent.{ExecutionContext, Future}

class StatManagerImpl(database: Connection, eventActions: EventActions)(implicit ec: ExecutionContext) extends StatManager {
  override def getPerDayPasses(perDayReportQuery: PerDayReportQuery): Future[VisitReport] = {
    eventActions.find(
      EventActions.GetEvents(
        aggregateId = Some(perDayReportQuery.userId),
        eventTypes = Some(Seq(EventType.UserEntered))
      )
    ).transactional(database)
      .map {
        _.groupBy(_.eventTime.withTimeAtStartOfDay())
          .values
          .flatMap(DayVisitReport.build)
      }
      .map(VisitReport.build)
  }

  override def getStat(userId: Long, interval: Interval): Future[Unit] = ???
}
