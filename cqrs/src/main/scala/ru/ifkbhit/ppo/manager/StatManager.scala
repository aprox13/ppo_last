package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.event.Interval
import ru.ifkbhit.ppo.model.stat.{PerDayReportQuery, VisitReport}

import scala.concurrent.Future

trait StatManager {

  def getPerDayPasses(query: PerDayReportQuery): Future[VisitReport]

  def getStat(userId: Long, interval: Interval): Future[Unit]
}
