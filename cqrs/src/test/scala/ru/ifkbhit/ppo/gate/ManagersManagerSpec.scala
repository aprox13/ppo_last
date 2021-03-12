package ru.ifkbhit.ppo.gate

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import ru.ifkbhit.ppo.actions.{DefaultManagerActions, ManagerActions}
import ru.ifkbhit.ppo.common.model.response.ResponseMatcher
import ru.ifkbhit.ppo.manager.ManagersManager
import ru.ifkbhit.ppo.manager.impl.ManagersManagerImpl
import ru.ifkbhit.ppo.model.event.{Event, EventType}
import ru.ifkbhit.ppo.model.manager.{UserPayload, UserResult}
import ru.ifkbhit.ppo.utils.BaseManagerSpec
import spray.json.enrichAny

import scala.concurrent.duration.DurationInt

class ManagersManagerSpec extends BaseManagerSpec with ResponseMatcher {

  private val managerActions: ManagerActions = new DefaultManagerActions(eventActions)

  private def managerManager: ManagersManager = new ManagersManagerImpl(connection, eventActions, managerActions)

  "ManagersManager" should {

    "correctly add users" in {
      timer.useRealNow()

      val payload1 = UserPayload("user1")
      val payload2 = UserPayload("user2")

      withSuccessTransaction {
        managerManager.addUser(payload1)
          .futureValue shouldBe successfulResponse(UserResult(1, payload1.name, passExpire = None, isPassActive = false))
      }
      withSuccessTransaction {
        managerManager.addUser(payload2)
          .futureValue shouldBe successfulResponse(UserResult(2, payload2.name, passExpire = None, isPassActive = false))
      }
      withSuccessTransaction {
        managerManager.addUser(payload1)
          .futureValue shouldBe successfulResponse(UserResult(3, payload1.name, passExpire = None, isPassActive = false))
      }

      val expectedInDatabase = Seq(
        Event(
          eventId = 1,
          aggregateId = 1,
          eventType = EventType.CreateUser,
          data = payload1.toJson,
          eventTime = timer.now()
        ),
        Event(
          eventId = 2,
          aggregateId = 2,
          eventType = EventType.CreateUser,
          data = payload2.toJson,
          eventTime = timer.now()
        ),
        Event(
          eventId = 3,
          aggregateId = 3,
          eventType = EventType.CreateUser,
          data = payload1.toJson,
          eventTime = timer.now()
        )
      )

      database.get() should contain theSameElementsInOrderAs expectedInDatabase

    }

    "return error on non existing user" in {
      timer.useRealNow()

      withFailedTransaction {
        managerManager.getUser(1111).futureValue shouldBe failedResponse("User not found: id=1111")
      }
    }

    "correctly get existing user" when {

      "he has no pass" in {
        timer.useRealNow()

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(UserResult(id = 1, name = "user1", passExpire = None, isPassActive = false))
        }
      }

      "he has active pass" in {
        val timePoint = DateTime.now().minusDays(10)

        timer.setNow(timePoint)

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        timer.tick(1.day)

        val expectedResult =
          UserResult(
            id = 1,
            name = "user1",
            passExpire = Some(timePoint.withTimeAtStartOfDay().plusDays(5)),
            isPassActive = true
          )

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(expectedResult)
        }
      }

      "he has expired pass" in {
        val timePoint = DateTime.now().minusDays(10)

        timer.setNow(timePoint)

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        timer.tick(7.day)

        val expectedResult =
          UserResult(
            id = 1,
            name = "user1",
            passExpire = Some(timePoint.withTimeAtStartOfDay().plusDays(5)),
            isPassActive = false
          )

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(expectedResult)
        }
      }
    }

    "correctly renew pass" when {

      "user has no pass" in {
        timer.useRealNow()

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        val expected =
          UserResult(
            id = 1,
            name = "user1",
            passExpire = Some(timer.now().withTimeAtStartOfDay().plusDays(5)),
            isPassActive = true
          )

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(expected)
        }
      }

      "user has expired pass" in {
        timer.setNow(DateTime.now().minusDays(10))

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        timer.tick(6.days)

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        val expected =
          UserResult(
            id = 1,
            name = "user1",
            passExpire = Some(timer.now().withTimeAtStartOfDay().plusDays(5)),
            isPassActive = true
          )

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(expected)
        }
      }

      "user has partially expired pass" in {
        timer.setNow(DateTime.now().minusDays(10))

        val user = withSuccessTransaction {
          managerManager.addUser(UserPayload("user1")).futureValue.as[UserResult]
        }

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        timer.tick(3.days)

        withSuccessTransaction {
          managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        }

        val expected =
          UserResult(
            id = 1,
            name = "user1",
            passExpire = Some(timer.now().withTimeAtStartOfDay().plusDays(5 + (5 - 3))),
            isPassActive = true
          )

        withSuccessTransaction {
          managerManager.getUser(user.id)
            .futureValue shouldBe successfulResponse(expected)
        }
      }
    }

    "return error when renew not existing user" in {
      timer.useRealNow()

      withFailedTransaction {
        managerManager.renewPass(1111, 5).futureValue shouldBe failedResponse("User not found: id=1111")
      }

    }
  }

}
