package ru.ifkbhit.ppo.manager.stat

import org.joda.time.DateTime
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.manager.StatManager
import ru.ifkbhit.ppo.manager.StatManager.NoStatFound
import ru.ifkbhit.ppo.model.event.ClosedInterval
import ru.ifkbhit.ppo.model.stat.{DayVisitsReport, FrequencyReport, StatReport, VisitReport}

import scala.concurrent.duration.DurationLong


class UserStatStorage(perUser: Map[Long, Seq[ClosedInterval]]) {

  def getPerDayStat(userId: Long): VisitReport =
    perUser
      .getOrElse(userId, throw NoStatFound(userId))
      .groupBy(_.from.withTimeAtStartOfDay())
      .values
      .flatMap(DayVisitsReport.build)
      .applyTransform(VisitReport.build)


  def getStat(
    userId: Long,
    frequency: FrequencyReport.Frequency,
    dateFrom: DateTime,
    dateTo: DateTime
  ): StatReport = {

    val interval = ClosedInterval(
      dateFrom.withTimeAtStartOfDay(),
      dateTo.withTimeAtStartOfDay()
        .plusDays(1)
        .minusMillis(1)
    )

    if (interval.duration.toMillis < frequency.periodMillis)
      throw StatManager.BadFrequencyInterval

    val targetVisits = perUser
      .getOrElse(userId, throw NoStatFound(userId))
      .groupBy(_.duration.toDays)
      .values
      .flatten

    val spentTotalMinutes = targetVisits.map(_.duration).map(_.toMinutes).sum

    val resultInterval =
      interval
        .applyTransform(_.copy(from = interval.from.withTimeAtStartOfDay()))
        .applyTransform(_.copy(to = interval.to.withTimeAtStartOfDay().plusDays(1)))


    println(resultInterval.duration.toDays)
    println(resultInterval.duration - resultInterval.duration.toDays.days)


    StatReport(
      totalCount = targetVisits.size,
      frequency = FrequencyReport(
        value =
          resultInterval.duration.toMillis.toDouble / frequency.periodMillis.toDouble,
        per = frequency
      ),
      averageSpentMinutes = (spentTotalMinutes / targetVisits.size).toInt,
      interval = resultInterval
    )
  }
}
