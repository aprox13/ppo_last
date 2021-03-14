package ru.ifkbhit.ppo.stat

import org.scalatest.matchers.BeMatcher
import ru.ifkbhit.ppo.actions.DefaultManagerActions
import ru.ifkbhit.ppo.manager.StatManager.{BadFrequencyInterval, NoStatFound}
import ru.ifkbhit.ppo.manager.impl.{GateManagerImpl, ManagersManagerImpl, StatManagerImpl}
import ru.ifkbhit.ppo.manager.stat.StatStorageProvider
import ru.ifkbhit.ppo.model
import ru.ifkbhit.ppo.model.event.{ClosedInterval, FromInterval, Interval}
import ru.ifkbhit.ppo.model.gate.{UserEnterCommand, UserExitCommand}
import ru.ifkbhit.ppo.model.manager.{RenewPassCommand, UserResult}
import ru.ifkbhit.ppo.model.stat.FrequencyReport.Frequency
import ru.ifkbhit.ppo.model.stat._
import ru.ifkbhit.ppo.utils.BaseManagerSpec

import scala.concurrent.Future
import scala.concurrent.duration._

class StatManagerSpec extends BaseManagerSpec {

  private def statStorageProvider = new StatStorageProvider(connection, eventActions)

  private def managerActions = new DefaultManagerActions(eventActions)

  private def managersManager = new ManagersManagerImpl(connection, eventActions, managerActions)

  private def gateManager = new GateManagerImpl(connection, eventActions, managerActions)

  private def manager = new StatManagerImpl(statStorageProvider)

  private val UserId: Long = 1
  private val UserPayload: model.manager.UserPayload = model.manager.UserPayload("user1")

  private def createUser: Unit =
    withSuccessTransaction {
      managersManager.addUser(payload = UserPayload) shouldBe successFuture(
        UserResult(UserId, UserPayload.name, None, isPassActive = false)
      )
    }

  private def renewPass(days: Int): Unit = {
    withSuccessTransaction {
      managersManager.renewPass(RenewPassCommand(UserId, days)) shouldBe successFuture
    }
  }

  private def enterUser: Unit =
    withSuccessTransaction {
      gateManager.enter(UserEnterCommand(UserId)) shouldBe successFuture
    }

  private def exitUser: Unit =
    withSuccessTransaction {
      gateManager.exit(UserExitCommand(UserId)) shouldBe successFuture
    }

  private def setupNow(hours: Int = 0, minutes: Int = 0): Unit = {
    timer.tick(-timer.now().getMillisOfDay.millis)
    timer.tick(hours.hours + minutes.minutes)
  }


  "StatManager" should {
    "correctly return stat per day" when {

      val NotStatFound = failureFuture(NoStatFound(UserId))

      def statShouldBe(matcher: BeMatcher[Future[VisitReport]]) = {
        withSuccessTransaction {
          manager.getPerDayPasses(PerDayReportQuery(UserId)) shouldBe matcher
        }
      }

      "come not existing user" in {
        withSuccessTransaction {
          manager.getPerDayPasses(PerDayReportQuery(userId = 1111)) shouldBe failureFuture(NoStatFound(1111))
        }
      }

      "come user without stat" in {
        createUser

        statShouldBe(NotStatFound)
        renewPass(5)
        statShouldBe(NotStatFound)
        enterUser
        statShouldBe(successFuture(VisitReport(0, Seq.empty)))

      }

      "come user with 1 visit" in {

        setupNow(15, 20)
        createUser

        renewPass(5)
        enterUser
        timer.tick(1.hour)
        exitUser

        val expectedVisit = DayVisitsReport(
          date = timer.now().withTimeAtStartOfDay(),
          count = 1,
          visits = Seq(
            OneVisitReport(
              timer.now().minusHours(1),
              minutes = 60
            )
          )
        )

        statShouldBe(successFuture(VisitReport(1, Seq(expectedVisit))))
      }


      "come user with 2 visit in day" in {

        setupNow(8, 30)
        createUser

        renewPass(5)
        enterUser
        timer.tick(1.hour)
        exitUser

        setupNow(16, 20)
        enterUser
        timer.tick(30.minutes)
        exitUser

        val expectedVisits = DayVisitsReport(
          date = timer.now().withTimeAtStartOfDay(),
          count = 2,
          visits = Seq(
            OneVisitReport(
              timer.now().withTimeAtStartOfDay()
                .withHourOfDay(8)
                .withMinuteOfHour(30),
              minutes = 60
            ),
            OneVisitReport(
              timer.now().withTimeAtStartOfDay()
                .withHourOfDay(16)
                .withMinuteOfHour(20),
              minutes = 30
            )
          )
        )

        statShouldBe(successFuture(VisitReport(2, Seq(expectedVisits))))
      }

      "come user with 2 visit in distinct days" in {

        setupNow(8, 30)
        createUser

        renewPass(5)
        enterUser
        timer.tick(1.hour)
        exitUser

        timer.tick(1.day - 60.minutes)
        enterUser
        timer.tick(30.minutes)
        exitUser

        val firstDay = DayVisitsReport(
          date = timer.now().withTimeAtStartOfDay().minusDays(1),
          count = 1,
          visits = Seq(
            OneVisitReport(
              timer.now().withTimeAtStartOfDay()
                .withHourOfDay(8)
                .withMinuteOfHour(30)
                .minusDays(1),
              minutes = 60
            )
          )
        )

        val secondDay = DayVisitsReport(
          date = timer.now().withTimeAtStartOfDay(),
          count = 1,
          visits = Seq(
            OneVisitReport(
              timer.now().withTimeAtStartOfDay()
                .withHourOfDay(8)
                .withMinuteOfHour(30),
              minutes = 30
            )
          )
        )

        statShouldBe(successFuture(VisitReport(2, Seq(firstDay, secondDay))))
      }

    }

    "correctly return stats" when {

      def stats(statQuery: StatQuery) =
        withSuccessTransaction {
          manager.getStat(
            statQuery
          )
        }

      def query(interval: Interval, frequency: Frequency) =
        StatQuery(UserId, interval, frequency)

      "comes bad frequency" in {
        createUser

        stats(query(ClosedInterval(timer.now(), timer.now().plusDays(1)), Frequency.Week)) shouldBe failureFuture(BadFrequencyInterval)
      }

      "comes user with visits" in {
        createUser
        renewPass(30)

        setupNow(9, 30)
        enterUser
        timer.tick(1.hour)
        exitUser

        timer.tick(2.days - 1.hour)

        enterUser
        timer.tick(1.hour)
        exitUser

        timer.tick(5.days - 1.hour)

        enterUser
        timer.tick(1.hour)
        exitUser

        timer.tick(2.days - 1.hour)

        enterUser
        timer.tick(1.hour)
        exitUser

        val from = timer.now().minusDays(13)

        stats(query(FromInterval(from), Frequency.Week)) shouldBe failureFuture(NoStatFound(1))
      }


    }

  }

}
