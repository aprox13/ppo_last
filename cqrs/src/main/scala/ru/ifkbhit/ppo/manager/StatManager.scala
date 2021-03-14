package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.model.stat.{PerDayReportQuery, StatQuery, StatReport, VisitReport}

import scala.concurrent.Future

trait StatManager {

  def getPerDayPasses(query: PerDayReportQuery): Future[VisitReport]

  def getStat(query: StatQuery): Future[StatReport]
}


object StatManager {

  case class NoStatFound(userId: Long) extends RuntimeException(s"No stat found for user $userId")

  case object BadFrequencyInterval extends RuntimeException("Frequency group more than interval")

}