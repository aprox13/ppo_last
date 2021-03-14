package ru.ifkbhit.ppo.manager.impl

import ru.ifkbhit.ppo.common.provider.Provider
import ru.ifkbhit.ppo.manager.StatManager
import ru.ifkbhit.ppo.manager.stat.UserStatStorage
import ru.ifkbhit.ppo.model.event.{ClosedInterval, FromInterval}
import ru.ifkbhit.ppo.model.stat.{PerDayReportQuery, StatQuery, StatReport, VisitReport}
import ru.ifkbhit.ppo.util.TimeProvider

import scala.concurrent.{ExecutionContext, Future}

class StatManagerImpl(statStorageProvider: Provider[UserStatStorage])(implicit ec: ExecutionContext, timeProvider: TimeProvider) extends StatManager {

  override def getPerDayPasses(perDayReportQuery: PerDayReportQuery): Future[VisitReport] =
    Future {
      statStorageProvider.get
        .getPerDayStat(perDayReportQuery.userId)
    }

  override def getStat(query: StatQuery): Future[StatReport] = Future {
    val (from, to) = query.interval match {
      case FromInterval(from) =>
        (from, timeProvider.now())
      case ClosedInterval(from, to) =>
        (from, to)
      case _ =>
        throw new RuntimeException("From is not specified")
    }

    statStorageProvider.get
      .getStat(query.userId, query.frequency, from, to)
  }
}
